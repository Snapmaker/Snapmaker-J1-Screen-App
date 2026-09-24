package fabscreen.platform.base.lib.api;

import fabscreen.platform.base.lib.VersionResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface J1APIService {
    @GET("/v1/j1/version")
    Observable<VersionResponse> getLatestVersion();
}
