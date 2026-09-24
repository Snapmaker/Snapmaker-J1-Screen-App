package fabscreen.platform.base.lib.api;

import fabscreen.platform.base.lib.VersionResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface A400APIService {
    @GET("/v1/a400/version")
    Observable<VersionResponse> getLatestVersion();
}
