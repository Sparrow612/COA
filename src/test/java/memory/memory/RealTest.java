package memory.memory;

import cpu.MMU;
import memory.Disk;
import memory.Memory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 实模式读取数据，逻辑地址(48-bits)等于物理地址(32-bits)
 */
public class RealTest {

	static MMU mmu;

	static Memory memory;

	static MemTestHelper helper;

	@BeforeClass
	public static void init() {
		mmu = MMU.getMMU();
		Memory.PAGE = false;
		Memory.SEGMENT = false;
		memory = Memory.getMemory();
		helper = new MemTestHelper();
	}

	@Test
	public void test1() {
		int len = 128;
		char[] data = helper.fillData((char)0b00001111, 128);
		assertArrayEquals(data, mmu.read("000000000000000000000000000000000000000000000000", len));
	}

	@Test
	public void test2() {
		String eip = "00000000000000000000000000000000";
		int len = 128;
		char[] data = helper.fillData((char)0b00001111, 128);
		assertArrayEquals(data, mmu.read("000000000000000000000000000000000000000000000000", len));
		data = helper.fillData((char)0b00000011, 128);
		Disk.getDisk().write(eip, len, data);
		assertArrayEquals(data, mmu.read("000000000000000000000000000000000000000000000000", len));
	}

	@After
	public void after() {
		// test2会写磁盘
		Disk.getDisk().write("00000000000000000000000000000000", 128, helper.fillData((char)0b00001111, 128));
		helper.clearAll();
	}

}
