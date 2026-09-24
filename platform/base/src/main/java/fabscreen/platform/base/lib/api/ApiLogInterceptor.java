package fabscreen.platform.base.lib.api;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiLogInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(chain.request());
        MediaType mediaType = null;
        String content = "";
        if (response.body() != null) {
            mediaType = response.body().contentType();
            content = response.body().string();
            Logger.d("API response: %s", content);
        } else {
            Logger.d("Response body is null");
        }
        return response.newBuilder().body(ResponseBody.create(mediaType, content)).build();
    }
}
