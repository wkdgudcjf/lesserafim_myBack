package com.lesserafim.config.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Manager {
		
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
     
    public String upload(MultipartFile multipartFile, String fileName) {
        ObjectMetadata objectMetaData = new ObjectMetadata();
        objectMetaData.setContentLength(multipartFile.getSize());
        objectMetaData.setContentType(multipartFile.getContentType());
        try (InputStream inputStream = multipartFile.getInputStream()){
        	return putS3(inputStream, objectMetaData, fileName);
        } catch (IOException e) {
        	e.printStackTrace();
		}
        return "fail";
    }
 
    private String putS3(InputStream inputStream, ObjectMetadata objectMetaData, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetaData).withCannedAcl(CannedAccessControlList.Private));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }
 
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.debug("파일이 삭제되었습니다.");
        } else {
            log.debug("파일이 삭제되지 못했습니다.");
        }
    }
 
	public File downloadImage(String imageUrl,String fileName) {
		URL url;
		//읽기 객체
		InputStream is;
		//쓰기 객체
		OutputStream os;
		try {
			url = new URL(imageUrl);
			
			is = url.openStream();
			
			os = new FileOutputStream(fileName);
			
			byte[] buffer = new byte[1024*16];
			
			while (true) {
				int inputData = is.read(buffer);
				if(inputData == -1)break;
				os.write(buffer,0,inputData);
			}

			is.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new File(fileName);
	}
}
