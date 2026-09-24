package fabscreen.platform.base.legacy.server.http.handlers;


import static fabscreen.platform.base.legacy.remote.SessionManager.NULL_SESSION;

import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.remote.SessionManager;
import fabscreen.platform.base.service.IAppService;

public class BaseRequestHandler {

    protected boolean ensureConnection(HttpRequest request, HttpResponse response) {
        String token = request.getParameter("token");
        if (token == null) {
            response.setStatus(HttpResponse.SC_BAD_REQUEST);
            return false;
        }

        SessionManager.Session currentSession = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getCurrentSession();

        // check if current session
        if (currentSession != NULL_SESSION && currentSession.getToken().equals(token)) {
            if (currentSession.isGranted()) {
                // session is granted, success
                ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().accessCurrentSession();
                return true;
            } else {
                ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().accessCurrentSession();
                // If session is pending granted, return (204)
                response.setStatus(HttpResponse.SC_NO_CONTENT);
                return false;
            }
        } else {
            // If Auth doesn't exist, deny request (401)
            response.setBody(new StringBody("Machine is not connected yet."));
            response.setStatus(HttpResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
