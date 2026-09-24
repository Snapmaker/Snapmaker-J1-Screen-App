package fabscreen.platform.base.lib.api;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.VersionResponse;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private final S20APIService mS20APIService;
    private final A400APIService mA400APIService;
    private final J1APIService mJ1APIService;

    public ApiClient(String url) {
        // Base url must end with slash.
        if (!url.endsWith("/")) {
            url += "/";
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new ApiLogInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mS20APIService = retrofit.create(S20APIService.class);
        mA400APIService = retrofit.create(A400APIService.class);
        mJ1APIService = retrofit.create(J1APIService.class);
    }

    public Observable<VersionResponse> getLatestVersion() {
        // int productId = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().productId;
        // Logger.d("get latest version: product id is %d", productId);
        // Use packageName instead of productId to determine the api path, because
        String packageName = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getPackageName();
        switch (packageName) {
            case "com.snapmaker.fabscreen":
                return mS20APIService.getLatestVersion();
            case "com.snapmaker.a400":
                return mA400APIService.getLatestVersion();
            case "com.snapmaker.j1":
                return mJ1APIService.getLatestVersion();
            default:
                return Observable.error(new IllegalArgumentException("No product matched!"));
        }
    }
}
