package fabscreen.platform.base.lib.api;

import fabscreen.platform.base.lib.VersionResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface S20APIService {
    @GET("/v1/fabscreen/version")
    Observable<VersionResponse> getLatestVersion();
}
