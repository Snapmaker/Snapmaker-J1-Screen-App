package fabscreen.platform.base.lib;

import java.util.ArrayList;

public class VersionResponse {
    public int code = 0;
    public String msg = "";
    public VersionResponseData data = new VersionResponseData();

    public static class VersionResponseData {
        public NewVersionData new_version = new NewVersionData();
    }

    public static class NewVersionData {
        public String version = "";
        public String url = "";
        public VersionChangeLog change_log = new VersionChangeLog();
        public int package_size = 0;
        public ArrayList<String> summary = new ArrayList<>();
        // TODO: 2022/6/17 add release time to api
        public long release_time;
    }
}
