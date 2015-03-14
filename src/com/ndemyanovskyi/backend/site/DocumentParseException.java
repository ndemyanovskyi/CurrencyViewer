/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.site;

import java.io.IOException;
import java.util.Objects;
import org.jsoup.nodes.Document;

/**
 *
 * @author Назарій
 */
public class DocumentParseException extends IOException {
    
    private static final long serialVersionUID = 1689352194881982190L;
    
    private final Site site;
    private final Document document;

    public DocumentParseException(Site site, Document document) {
        this.site = Objects.requireNonNull(site, "site");
        this.document = Objects.requireNonNull(document, "document");
    }

    public DocumentParseException(Site site, Document document, String message) {
        super(message);
        this.site = Objects.requireNonNull(site, "site");
        this.document = Objects.requireNonNull(document, "document");
    }

    public DocumentParseException(Site site, Document document, Throwable cause) {
        super(cause);
        this.site = Objects.requireNonNull(site, "site");
        this.document = Objects.requireNonNull(document, "document");
    }

    public DocumentParseException(Site site, Document document, String message, Throwable cause) {
        super(message, cause);
        this.site = Objects.requireNonNull(site, "site");
        this.document = Objects.requireNonNull(document, "document");
    }

    public Document getDocument() {
        return document;
    }

    public Site getSite() {
        return site;
    }
    
}
