package com.team404.synco.common.util;

/**
 * 파일 확장자에 따른 Content-Type을 반환하는 유틸리티 클래스
 */
public class ContentTypeUtil {

    /**
     * 파일명의 확장자에 따라 적절한 Content-Type을 반환합니다.
     *
     * @param fileName 파일명
     * @return 해당 파일의 Content-Type
     */
    public static String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }

        String extension = fileName.toLowerCase();

        // 이미지 파일
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else if (extension.endsWith(".bmp")) {
            return "image/bmp";
        } else if (extension.endsWith(".webp")) {
            return "image/webp";
        } else if (extension.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (extension.endsWith(".ico")) {
            return "image/x-icon";
        } else if (extension.endsWith(".tiff") || extension.endsWith(".tif")) {
            return "image/tiff";
        }

        // 문서 파일
        else if (extension.endsWith(".pdf")) {
            return "application/pdf";
        } else if (extension.endsWith(".txt")) {
            return "text/plain";
        } else if (extension.endsWith(".rtf")) {
            return "application/rtf";
        } else if (extension.endsWith(".csv")) {
            return "text/csv";
        } else if (extension.endsWith(".xml")) {
            return "application/xml";
        } else if (extension.endsWith(".json")) {
            return "application/json";
        } else if (extension.endsWith(".html") || extension.endsWith(".htm")) {
            return "text/html";
        } else if (extension.endsWith(".css")) {
            return "text/css";
        } else if (extension.endsWith(".js")) {
            return "application/javascript";
        }

        // Microsoft Office 파일
        else if (extension.endsWith(".doc")) {
            return "application/msword";
        } else if (extension.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (extension.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (extension.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (extension.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (extension.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        // 압축 파일
        else if (extension.endsWith(".zip")) {
            return "application/zip";
        } else if (extension.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (extension.endsWith(".7z")) {
            return "application/x-7z-compressed";
        } else if (extension.endsWith(".tar")) {
            return "application/x-tar";
        } else if (extension.endsWith(".gz")) {
            return "application/gzip";
        }

        // 비디오 파일
        else if (extension.endsWith(".mp4")) {
            return "video/mp4";
        } else if (extension.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (extension.endsWith(".mov")) {
            return "video/quicktime";
        } else if (extension.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (extension.endsWith(".flv")) {
            return "video/x-flv";
        } else if (extension.endsWith(".webm")) {
            return "video/webm";
        } else if (extension.endsWith(".mkv")) {
            return "video/x-matroska";
        }

        // 오디오 파일
        else if (extension.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (extension.endsWith(".wav")) {
            return "audio/wav";
        } else if (extension.endsWith(".flac")) {
            return "audio/flac";
        } else if (extension.endsWith(".aac")) {
            return "audio/aac";
        } else if (extension.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (extension.endsWith(".m4a")) {
            return "audio/mp4";
        }

        // 프로그래밍 파일
        else if (extension.endsWith(".java")) {
            return "text/x-java-source";
        } else if (extension.endsWith(".py")) {
            return "text/x-python";
        } else if (extension.endsWith(".cpp") || extension.endsWith(".cc") || extension.endsWith(".cxx")) {
            return "text/x-c++";
        } else if (extension.endsWith(".c")) {
            return "text/x-c";
        } else if (extension.endsWith(".php")) {
            return "application/x-httpd-php";
        } else if (extension.endsWith(".rb")) {
            return "text/x-ruby";
        } else if (extension.endsWith(".go")) {
            return "text/x-go";
        } else if (extension.endsWith(".rs")) {
            return "text/x-rust";
        } else if (extension.endsWith(".swift")) {
            return "text/x-swift";
        } else if (extension.endsWith(".kt")) {
            return "text/x-kotlin";
        } else if (extension.endsWith(".scala")) {
            return "text/x-scala";
        } else if (extension.endsWith(".sh")) {
            return "application/x-sh";
        } else if (extension.endsWith(".bat")) {
            return "application/x-msdos-program";
        } else if (extension.endsWith(".ps1")) {
            return "application/x-powershell";
        }

        // 기타 파일
        else if (extension.endsWith(".exe")) {
            return "application/x-msdownload";
        } else if (extension.endsWith(".dmg")) {
            return "application/x-apple-diskimage";
        } else if (extension.endsWith(".iso")) {
            return "application/x-iso9660-image";
        } else if (extension.endsWith(".deb")) {
            return "application/x-debian-package";
        } else if (extension.endsWith(".rpm")) {
            return "application/x-rpm";
        } else if (extension.endsWith(".apk")) {
            return "application/vnd.android.package-archive";
        } else if (extension.endsWith(".ipa")) {
            return "application/octet-stream";
        }

        // 기본값
        else {
            return "application/octet-stream";
        }
    }
}
