package com.team404.synco.drive.util;

/**
 * 파일 확장자에 따른 Content-Type을 반환하는 유틸리티 클래스
 */
public final class ContentTypeUtil {

    // 생성자 숨김 (유틸리티 클래스)
    private ContentTypeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }

        String extension = fileName.toLowerCase();

        // 확장자 추출 (마지막 점 이후)
        int lastDotIndex = extension.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "application/octet-stream";
        }

        String ext = extension.substring(lastDotIndex);

        return switch (ext) {
            // 이미지 파일
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".bmp" -> "image/bmp";
            case ".webp" -> "image/webp";
            case ".svg" -> "image/svg+xml";
            case ".ico" -> "image/x-icon";
            case ".tiff", ".tif" -> "image/tiff";

            // 문서 파일
            case ".pdf" -> "application/pdf";
            case ".txt" -> "text/plain";
            case ".rtf" -> "application/rtf";
            case ".csv" -> "text/csv";
            case ".xml" -> "application/xml";
            case ".json" -> "application/json";
            case ".html", ".htm" -> "text/html";
            case ".css" -> "text/css";
            case ".js" -> "application/javascript";
            case ".yml", ".yaml" -> "application/x-yaml";

            // Microsoft Office 파일
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls" -> "application/vnd.ms-excel";
            case ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt" -> "application/vnd.ms-powerpoint";
            case ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            // 압축 파일
            case ".zip" -> "application/zip";
            case ".rar" -> "application/x-rar-compressed";
            case ".7z" -> "application/x-7z-compressed";
            case ".tar" -> "application/x-tar";
            case ".gz" -> "application/gzip";

            // 비디오 파일
            case ".mp4" -> "video/mp4";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            case ".wmv" -> "video/x-ms-wmv";
            case ".flv" -> "video/x-flv";
            case ".webm" -> "video/webm";
            case ".mkv" -> "video/x-matroska";

            // 오디오 파일
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".flac" -> "audio/flac";
            case ".aac" -> "audio/aac";
            case ".ogg" -> "audio/ogg";
            case ".m4a" -> "audio/mp4";

            // 프로그래밍 파일
            case ".java" -> "text/x-java-source";
            case ".py" -> "text/x-python";
            case ".cpp", ".cc", ".cxx" -> "text/x-c++";
            case ".c" -> "text/x-c";
            case ".php" -> "application/x-httpd-php";
            case ".rb" -> "text/x-ruby";
            case ".go" -> "text/x-go";
            case ".rs" -> "text/x-rust";
            case ".swift" -> "text/x-swift";
            case ".kt" -> "text/x-kotlin";
            case ".scala" -> "text/x-scala";
            case ".sh" -> "application/x-sh";
            case ".bat" -> "application/x-msdos-program";
            case ".ps1" -> "application/x-powershell";

            // 기타 파일
            case ".exe" -> "application/x-msdownload";
            case ".dmg" -> "application/x-apple-diskimage";
            case ".iso" -> "application/x-iso9660-image";
            case ".deb" -> "application/x-debian-package";
            case ".rpm" -> "application/x-rpm";
            case ".apk" -> "application/vnd.android.package-archive";
            case ".ipa" -> "application/octet-stream";

            // 기본값
            default -> "application/octet-stream";
        };
    }
}
