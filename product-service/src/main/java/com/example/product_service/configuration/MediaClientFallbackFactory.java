package com.example.product_service.configuration;

import com.example.product_service.dto.response.ListMediaResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.repository.httpsClient.MediaClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaClientFallbackFactory extends BaseFallbackFactory<MediaClient> {
    @Override
    protected String getClientName() {
        return "media-service";
    }

    @Override
    public MediaClient create(Throwable cause){
        super.create(cause);
        return new MediaClient() {
            @Override
            public ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(String ownerId, MediaOwnerType mediaOwnerType) {
                throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
            }
        };
    }
}
