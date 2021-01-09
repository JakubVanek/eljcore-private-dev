package fake_ev3dev.ev3dev.utils.io;

import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ev3dev.utils.io.NativeConstants;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Mock file looking like a regular text file.
 */
public class RegularFileMock implements IFile {
    private final byte[] buffer;
    private int filePtr;
    private int fileSize;
    private int fd;
    private final boolean readable;
    private final boolean writable;
    private boolean openForReading;
    private boolean openForWriting;

    /**
     * Create a new empty regular file with fixed capacity.
     *
     * @param capacity Initial capacity of the file.
     */
    public RegularFileMock(int capacity, boolean canRead, boolean canWrite) {
        this.buffer = new byte[capacity];
        this.filePtr = 0;
        this.fileSize = 0;
        this.readable = canRead;
        this.writable = canWrite;
        this.openForReading = false;
        this.openForWriting = false;
        this.fd = -1;
    }

    /**
     * Set mock file contents.
     * @param buffer Data to have in the file.
     */
    public void setContents(byte[] buffer) {
        int length = Math.min(this.buffer.length, buffer.length);
        System.arraycopy(buffer, 0, this.buffer, 0, length);
        filePtr = 0;
        fileSize = length;
    }

    public byte[] getContents() {
        byte[] output = new byte[fileSize];
        System.arraycopy(this.buffer, 0, output, 0, fileSize);
        return output;
    }

    @Override
    public int fcntl(int fd, int cmd, int arg) throws LastErrorException {
        throw new LastErrorException("fcntl not implemented");
    }

    @Override
    public int ioctl(int fd, int cmd, int arg) throws LastErrorException {
        return 0;
    }

    @Override
    public int ioctl(int fd, int cmd, Pointer arg) throws LastErrorException {
        return 0;
    }

    @Override
    public int open(String path, int flags, int mode) throws LastErrorException {
        throw new LastErrorException("non-emulated variant of open not supported");
    }

    @Override
    public int open(int fd, String path, int flags, int mode) throws LastErrorException {
        boolean read = false, write = false;
        if ((flags & 0x03) == NativeConstants.O_RDONLY) {
            read = true;
        } else if ((flags & 0x03) == NativeConstants.O_WRONLY) {
            write = true;
        } else if ((flags & 0x03) == NativeConstants.O_RDWR) {
            read = true;
            write = true;
        } else {
            throw new LastErrorException("invalid open mode");
        }

        if (write && !this.writable) {
            throw new LastErrorException("cannot open non-writable for for writing");
        }
        if (read && !this.readable) {
            throw new LastErrorException("cannot open non-writable for for writing");
        }

        if (write && (flags & NativeConstants.O_TRUNC) != 0) {
            this.filePtr = 0;
            this.fileSize = 0;
        } else if (write && (flags & NativeConstants.O_APPEND) != 0) {
            this.filePtr = this.fileSize;
        } else {
            this.filePtr = 0;
        }
        this.fd = fd;
        this.openForReading = read;
        this.openForWriting = write;
        return fd;
    }

    @Override
    public int close(int fd) throws LastErrorException {
        if (this.fd != fd) {
            throw new LastErrorException("unexpected fd");
        }
        this.fd = -1;
        this.openForWriting = false;
        this.openForReading = false;
        return 0;
    }

    @Override
    public int write(int fd, Buffer buffer, int count) throws LastErrorException {
        if (!openForWriting)
            throw new LastErrorException("cannot write file not opened for writing");
        if (!(buffer instanceof ByteBuffer))
            throw new LastErrorException("only bytebuffers are supported");
        if (!buffer.hasArray())
            throw new LastErrorException("only heap bytebuffers supported");

        ByteBuffer bbuf = (ByteBuffer) buffer;

        int realWrite = count;
        if (count + filePtr > this.buffer.length)
            realWrite = fileSize - this.buffer.length;
        System.arraycopy(bbuf.array(), bbuf.position(), this.buffer, filePtr, realWrite);
        filePtr += realWrite;
        if (filePtr > fileSize)
            fileSize = filePtr;
        return realWrite;
    }

    @Override
    public int read(int fd, Buffer buffer, int count) throws LastErrorException {
        if (!openForReading)
            throw new LastErrorException("cannot read file not opened for reading");
        if (!(buffer instanceof ByteBuffer))
            throw new LastErrorException("only bytebuffers are supported");
        if (!buffer.hasArray())
            throw new LastErrorException("only heap bytebuffers supported");

        ByteBuffer bbuf = (ByteBuffer) buffer;

        int realRead = count;
        if (count + filePtr > fileSize)
            realRead = fileSize - filePtr;
        System.arraycopy(this.buffer, filePtr, bbuf.array(), bbuf.position(), realRead);
        filePtr += realRead;
        return realRead;
    }

    @Override
    public Pointer mmap(Pointer addr, NativeLong len, int prot, int flags, int fd, NativeLong off) throws LastErrorException {
        return new Memory(len.longValue());
    }

    @Override
    public int munmap(Pointer addr, NativeLong len) throws LastErrorException {
        return 0;
    }

    @Override
    public int msync(Pointer addr, NativeLong len, int flags) throws LastErrorException {
        return 0;
    }
}
