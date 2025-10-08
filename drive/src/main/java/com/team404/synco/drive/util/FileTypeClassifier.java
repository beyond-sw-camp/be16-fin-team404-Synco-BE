package com.team404.synco.drive.util;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.Folder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileTypeClassifier {


    public static class FolderTypeInfo {
        public final String type = "folder";
        public final String size = "-";
        public final String icon = "mdi-folder";

        public static FolderTypeInfo of(Folder folder) {
            return new FolderTypeInfo();
        }
    }


    public static class DocumentTypeInfo {
        public final String type;
        public final String size;
        public final String icon;
        public final boolean isShared;
        public final boolean isLocked;

        private DocumentTypeInfo(String type, String size, String icon, boolean isShared, boolean isLocked) {
            this.type = type;
            this.size = size;
            this.icon = icon;
            this.isShared = isShared;
            this.isLocked = isLocked;
        }

        public static DocumentTypeInfo of(Document document) {
            boolean isShared = DocumentType.CUSTOM.equals(document.getDocumentType());
            boolean isLocked = document.getYnLock() != null && document.getYnLock().equals(YnColumn.IS_TRUE);
            
            return new DocumentTypeInfo(
                isShared ? "shared-doc" : "file",
                isShared ? "-" : "0 KB",
                isShared ? "mdi-file-document-multiple" : getFileIcon(document.getDocumentName()),
                isShared,
                isLocked
            );
        }

        private static String getFileIcon(String fileName) {
            if (fileName == null) return "mdi-file";
            
            String extension = getFileExtension(fileName).toLowerCase();
            return switch (extension) {
                case "pdf" -> "mdi-file-pdf";
                case "doc", "docx" -> "mdi-file-word";
                case "xls", "xlsx" -> "mdi-file-excel";
                case "ppt", "pptx" -> "mdi-file-powerpoint";
                case "jpg", "jpeg", "png", "gif" -> "mdi-file-image";
                case "txt" -> "mdi-file-document";
                default -> "mdi-file";
            };
        }

        private static String getFileExtension(String fileName) {
            int lastDotIndex = fileName.lastIndexOf(".");
            return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
        }
    }
}
