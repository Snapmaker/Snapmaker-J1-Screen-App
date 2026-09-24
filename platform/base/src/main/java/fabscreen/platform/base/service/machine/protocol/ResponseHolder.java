package fabscreen.platform.base.service.machine.protocol;

import fabscreen.platform.base.service.machine.IStructure;
import io.reactivex.subjects.BehaviorSubject;

public class ResponseHolder {
    private BehaviorSubject< IStructure> responseSubject;
    private IStructure responseStructure;

    public ResponseHolder(BehaviorSubject<IStructure> subject, IStructure structure) {
        this.responseSubject = subject;
        this.responseStructure = structure;
    }

    public BehaviorSubject<IStructure> getResponseSubject() {
        return responseSubject;
    }

    public void setResponseSubject(BehaviorSubject<IStructure> responseSubject) {
        this.responseSubject = responseSubject;
    }

    public IStructure getResponseStructure() {
        return responseStructure;
    }

    public void setResponseStructure(IStructure responseStructure) {
        this.responseStructure = responseStructure;
    }
}
