package memory.memory;

import cpu.MMU;
import memory.Memory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * 段页式内存管理模式下读数据，需要逻辑地址转线性地址再转物理地址
 */
public class PSTest {

	static MMU mmu;

	static Memory memory;

	static MemTestHelper helper;

	@BeforeClass
	public static void init() {
		mmu = MMU.getMMU();
		Memory.PAGE = true;
		Memory.SEGMENT = true;
		memory = Memory.getMemory();
		helper = new MemTestHelper();
	}

	@Test
	public void test1() {
		String eip = "00000000000000000000000000000000";
		int len = 2 * 1024;
		char[] data = helper.fillData((char)0b00001111, len);
		memory.alloc_seg_force(0, eip, len / 2, true, "");
		assertArrayEquals(data, mmu.read("000000000000000000000000000000000000000000000000", len));
	}


	/**
	 * Situation: 段不在内存 + 页替换
	 */
	@Test
	public void test2() {
		String eip = "00000000000000000000000000000000";
		int len = 2 * 1024;
		char[] data = helper.fillData((char)0b00001111, len);
		memory.alloc_seg_force(0, eip, len / 2, false, "");
//		memory.invalid(0, -1);
		assertArrayEquals(data, mmu.read("000000000000000000000000000000000000000000000000", len));
	}


	/**
	 * Situation: 段替换
	 */
	@Test
	public void test3() {
		int len1 = 20 * 1024 * 1024;
		int len2 = 32 * 1024 * 1024;
		int len3 = 16 * 1024 * 1024;
		char[] data1 = helper.fillData((char)0b00001111, 77);	// 磁盘存储位置[0M-20M)
//		char[] data2 = helper.fillData((char)0b01010101, len2);	// 磁盘存储位置[32M-64M)
		char[] data3 = helper.fillData((char)0b00110011, 1025 * 1024);	// 磁盘存储位置[64M-80M)
		// 初始化内存(10 16 -6)
		String eip1 = "00000000000000000000000000000000";
		String eip2 = "00000000101000000000000000000000";
		String eip3 = "00000001101000000000000000000000";
		memory.alloc_seg_force(0, eip1, len1 / 2, true, "");
		memory.alloc_seg_force(1, eip2, len2 / 2, true, "");
		memory.alloc_seg_force(2, eip3, len3 / 2, false, "");
//		memory.invalid(2, -1);
		// 读取第三个段(替换第一个段)(内存状态8 16 -8)
		assertArrayEquals(data3, mmu.read("000000000001000000010000000000000000000000000000", 1025 * 1024));
		// 段三直接移除内存(内存状态-8 16 -8)
		memory.invalid(2, -1);
		// 读取第一个段(内存状态16 10 -6)
		assertArrayEquals(data1, mmu.read("000000000000000000000000000000000000000000000000", 77));
	}

	@Test
	public void test4() {
		int len = 2 * 1024;
		char[] data_fraction1 = helper.fillData((char)0b00001111, len / 2);	// 磁盘存储位置[19.999M-20M)
		char[] data_fraction2 = helper.fillData((char)0b00000011, len / 2);	// 磁盘存储位置[20M-20.001M)
		memory.alloc_seg_force(0, "00000000000000000000000000000000", len / 2, false, "");
		assertArrayEquals(data_fraction1, mmu.read("000000000000000000000100111111111111000000000000", len / 2));
		assertArrayEquals(data_fraction2, mmu.read("000000000000000000000101000000000000000000000000", len / 2));
		assertArrayEquals(data_fraction1, mmu.read("000000000000000000000100111111111111000000000000", len / 2));
		assertArrayEquals(data_fraction2, mmu.read("000000000000000000000101000000000000000000000000", len / 2));
	}


	@After
	public void after() {
		helper.clearAll();
	}

}
