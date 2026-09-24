package fabscreen.platform.base.service.machine;

import java.io.IOException;

import okio.Buffer;

public interface IStructure {
    byte[] toByteArray();

    Buffer readBuffer(Buffer buffer) throws IOException;

    String toString();
}
