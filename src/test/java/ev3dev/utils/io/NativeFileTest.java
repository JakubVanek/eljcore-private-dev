package ev3dev.utils.io;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import fake_ev3dev.ev3dev.utils.io.CountingFile;
import fake_ev3dev.ev3dev.utils.io.EmulatedLibc;
import fake_ev3dev.ev3dev.utils.io.RegularFileMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NativeFileTest {
    private EmulatedLibc emulatedLibc = null;
    private CountingFile callCounter = null;
    private RegularFileMock mock = null;
    private String path;
    private byte[] contents;

    @Before
    public void reset() {
        path = "/dev/test";
        contents = new byte[]{'h', 'e', 'l', 'l', 'o'};
        emulatedLibc = new EmulatedLibc();
        mock = new RegularFileMock(4096, true, true);
        mock.setContents(contents);
        callCounter = new CountingFile(mock);
        emulatedLibc.install(path, callCounter);
    }

    @Test
    public void openTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            Assert.assertTrue(file.isOpen());
        }
    }

    @Test
    public void readArrayTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            byte[] test = new byte[5];
            file.read(test, 0, 5);
            Assert.assertArrayEquals(test, contents);
            Assert.assertEquals(callCounter.getCountRead(), 1);
        }
    }

    @Test
    public void writeArrayTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_WRONLY | NativeConstants.O_TRUNC,
            0, emulatedLibc)) {
            byte[] test = new byte[]{'t', 'e', 's', 't'};
            file.write(test, 0, 4);
            Assert.assertArrayEquals(mock.getContents(), test);
            Assert.assertEquals(callCounter.getCountWrite(), 1);
        }
    }

    @Test
    public void readArrayAtOffsetTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            byte[] test = new byte[5];
            test[0] = ' ';
            file.read(test, 1, 4);
            Assert.assertArrayEquals(test, new byte[]{' ', 'e', 'l', 'l', 'o'});
            Assert.assertEquals(callCounter.getCountRead(), 1);
        }
    }

    @Test
    public void writeArrayAtOffsetTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_WRONLY | NativeConstants.O_TRUNC,
            0, emulatedLibc)) {
            byte[] test = new byte[]{' ', 't', 'e', 's', 't'};
            file.write(test, 1, 4);
            Assert.assertArrayEquals(mock.getContents(), new byte[]{'t', 'e', 's', 't'});
            Assert.assertEquals(callCounter.getCountWrite(), 1);
        }
    }

    @Test
    public void ioctlIntPtrTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            IntByReference ref = new IntByReference(0);
            file.ioctl(0, ref);
            Assert.assertEquals(callCounter.getCountIoctl_ptr(), 1);
        }
    }

    @Test
    public void ioctlIntTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            file.ioctl(0, 0);
            Assert.assertEquals(callCounter.getCountIoctl_int(), 1);
        }
    }

    @Test
    public void ioctlMemPtrTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            file.ioctl(0, new Memory(1));
            Assert.assertEquals(callCounter.getCountIoctl_ptr(), 1);
        }
    }

    @Test
    public void mmapTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            file.mmap(1, 0, 0, 0);
            Assert.assertEquals(callCounter.getCountMmap(), 1);
        }
    }

    @Test
    public void munmapTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            Pointer memory = file.mmap(1, 0, 0, 0);
            file.munmap(memory, 0);
        }
    }

    @Test
    public void msyncTest() {
        try (NativeFile file = new NativeFile(path, NativeConstants.O_RDONLY, 0, emulatedLibc)) {
            Pointer memory = file.mmap(1, 0, 0, 0);
            file.msync(memory, 0, 0);
        }
    }
}
