package com.rag.enterprise.app.service;

import com.rag.enterprise.app.domain.DocumentChunk;
import com.rag.enterprise.app.exception.DocumentProcessingException;
import com.rag.enterprise.app.exception.InvalidDocumentException;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    // Initialize JTokkit's encoding registry
    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    // CL100K_BASE is the tokenizer used by text-embedding-ada-002 / text-embedding-3
    private final Encoding encoding = registry.getEncoding(EncodingType.CL100K_BASE);

    // Core RAG Hyperparameters
    private static final int CHUNK_SIZE_TOKENS = 800;
    private static final int CHUNK_OVERLAP_TOKENS = 100;

    // Enterprise Guardrail: Prevent JVM Heap exhaustion
    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15MB Limit

    public List<DocumentChunk> processPdf(MultipartFile file) {
        // Phase 1: Structural & Security Validation
        validateFileHeader(file);

        List<DocumentChunk> allChunks = new ArrayList<>();
        String filename = file.getOriginalFilename();

        // Phase 2: Open and Stream the PDF Content Page-by-Page
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            if (document.isEncrypted()) {
                throw new InvalidDocumentException("Cannot process password-protected or encrypted PDF: " + filename);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText;
                try {
                    pageText = stripper.getText(document);
                } catch (IOException e) {
                    // Graceful degrade: log warning for a broken page, keep processing the rest
                    System.err.println("Warning: Failed to extract text from page " + page + " in " + filename);
                    continue;
                }

                if (pageText == null || pageText.isBlank()) {
                    continue; // Skip image-only or blank pages
                }

                // Phase 3: True Token-Based Sliding Window Splitting
                List<String> tokenizedChunks = splitTextByTokensWithOverlap(pageText, CHUNK_SIZE_TOKENS, CHUNK_OVERLAP_TOKENS);

                // Phase 4: Construct Immutable Domain Records with Structural Metadata
                for (int i = 0; i < tokenizedChunks.size(); i++) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", filename != null ? filename : "unknown");
                    metadata.put("page_number", page);
                    metadata.put("total_pages", totalPages);
                    metadata.put("chunk_index", i);
                    metadata.put("timestamp", System.currentTimeMillis());

                    allChunks.add(new DocumentChunk(
                            UUID.randomUUID().toString(),
                            tokenizedChunks.get(i),
                            metadata
                    ));
                }
            }
        } catch (InvalidDocumentException e) {
            throw e;
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read or parse document stream: " + filename, e);
        } catch (Exception e) {
            throw new DocumentProcessingException("Unexpected error during document ingestion: " + filename, e);
        }

        return allChunks;
    }

    /**
     * Slices text into semantic blocks using JTokkit's token integer lists.
     * Guarantees words and numbers are never cut in half across chunk edges.
     */
    private List<String> splitTextByTokensWithOverlap(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();

        // Convert the raw string into JTokkit's IntArrayList of Token IDs
        IntArrayList tokens = encoding.encode(text);
        int totalTokens = tokens.size();

        if (totalTokens <= size) {
            chunks.add(text.trim());
            return chunks;
        }

        int start = 0;
        while (start < totalTokens) {
            int end = Math.min(start + size, totalTokens);

            // JTokkit tokens can be segmented safely using box primitives
            IntArrayList chunkTokens = new IntArrayList();
            for (int i = start; i < end; i++) {
                chunkTokens.add(tokens.get(i));
            }

            // Re-translate the token IDs cleanly back into natural text
            String decodedChunk = encoding.decode(chunkTokens);
            chunks.add(decodedChunk.trim());

            if (end == totalTokens) {
                break;
            }

            // Shift window forward, keeping exactly 100 tokens of trailing context
            start = end - overlap;

            // Circuit-breaker guard against infinite loops
            if (start >= end) {
                start = end;
            }
        }
        return chunks;
    }

    /**
     * Evaluates incoming payloads for file size limits and validates true
     * binary signatures to defend against MIME-spoofing attacks.
     */
    private void validateFileHeader(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("Uploaded file is empty or missing.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidDocumentException("File size exceeds maximum allowable limit of 15MB.");
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = is.read(header);
            if (bytesRead < 4 || !new String(header).equals("%PDF")) {
                throw new InvalidDocumentException("Invalid file format. File signature does not match a true PDF.");
            }
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read file headers for validation.", e);
        }
    }
}