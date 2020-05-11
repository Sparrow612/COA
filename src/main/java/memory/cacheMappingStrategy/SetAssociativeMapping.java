package memory.cacheMappingStrategy;

import memory.Cache;
import memory.Memory;
import transformer.Transformer;

/**
 * 4路-组相连映射 n=4,   14位标记 + 8位组号 + 10位块内地址
 * 256个组，每个组4行
 */
public class SetAssociativeMapping extends MappingStrategy{

    static final int SETS = 256; // 共256个组
    static final int setSize = 4;   // 每个组4行

    Transformer t = new Transformer();

    /**
     *
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前14位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        int tag = blockNO / SETS;
        String tagStr = t.intToBinary( ""+tag ).substring( 18,32 );
        int diff = 22 - tagStr.length();
        for(int i=0;i<diff; i++){
            tagStr = tagStr +"0";
        }
        return tagStr.toCharArray();
    }

    /**
     *
     * @param blockNO 目标数据内存地址前22位int表示
     * @return -1 表示未命中
     */
    @Override
    public int map(int blockNO) {
        int setNO = blockNO % SETS;           // 获得内存地址blockNO所对应的组号setNO
        char[] addrTag = getTag( blockNO );   // 获得内存地址blockNO所对应的tag
        return this.replacementStrategy.isHit( setNO*setSize, (setNO+1)*setSize-1, addrTag );
    }

    @Override
    public int writeCache(int blockNO) {
        int setNO = blockNO % SETS;
        char[] addrTag = getTag( blockNO );
        return this.replacementStrategy.writeCache(setNO*setSize, (setNO+1)*setSize-1, addrTag , Memory.getMemory().read(t.intToBinary(String.valueOf(Cache.LINE_SIZE_B * blockNO)), Cache.LINE_SIZE_B));
    }
}










