package com.facebook.presto.ingest;

import com.facebook.presto.TupleInfo;
import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.Cursor;
import com.facebook.presto.block.uncompressed.UncompressedBlock;
import com.facebook.presto.block.uncompressed.UncompressedCursor;
import com.facebook.presto.slice.Slice;
import com.google.common.collect.AbstractIterator;

import java.io.Closeable;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class RowSourceBuilder
        implements RowSource, Iterable<UncompressedBlock>
{
    public static interface RowBuilder
    {
        RowBuilder append(long value);

        RowBuilder append(double value);

        RowBuilder append(byte[] value);

        RowBuilder append(Slice value);
    }

    public static interface RowGenerator
        extends Closeable
    {
        boolean generate(RowBuilder rowBuilder);
    }

    private final TupleInfo tupleInfo;
    private final RowGenerator rowGenerator;
    private boolean cursorCreated = false;

    public RowSourceBuilder(TupleInfo tupleInfo, RowGenerator rowGenerator)
    {
        this.tupleInfo = checkNotNull(tupleInfo, "tupleInfo is null");
        this.rowGenerator = checkNotNull(rowGenerator, "rowGenerator is null");
    }

    @Override
    public TupleInfo getTupleInfo()
    {
        return tupleInfo;
    }

    @Override
    public Cursor cursor()
    {
        checkState(!cursorCreated, "cursor already created");
        cursorCreated = true;
        return new UncompressedCursor(tupleInfo, iterator());
    }

    @Override
    public RowSourceIterator iterator()
    {
        return new RowSourceIterator();
    }

    @Override
    public void close()
            throws IOException
    {
        rowGenerator.close();
    }

    private class RowSourceIterator
            extends AbstractIterator<UncompressedBlock>
    {
        private boolean done = false;
        private int position = 0;

        @Override
        protected UncompressedBlock computeNext()
        {
            if (done) {
                return endOfData();
            }

            BlockBuilder blockBuilder = new BlockBuilder(position, tupleInfo);
            BasicRowBuilder rowBuilder = new BasicRowBuilder(blockBuilder);

            do {
                if (!rowGenerator.generate(rowBuilder)) {
                    done = true;
                    if (blockBuilder.isEmpty()) {
                        return endOfData();
                    }
                    break;
                }
            }
            while (!blockBuilder.isFull());

            UncompressedBlock block = blockBuilder.build();
            position += block.getCount();
            return block;
        }
    }

    private static class BasicRowBuilder
            implements RowBuilder
    {
        private final BlockBuilder blockBuilder;

        private BasicRowBuilder(BlockBuilder blockBuilder)
        {
            this.blockBuilder = blockBuilder;
        }

        @Override
        public RowBuilder append(long value)
        {
            blockBuilder.append(value);
            return this;
        }

        @Override
        public RowBuilder append(double value)
        {
            blockBuilder.append(value);
            return this;
        }

        @Override
        public RowBuilder append(byte[] value)
        {
            blockBuilder.append(value);
            return this;
        }

        @Override
        public RowBuilder append(Slice value)
        {
            blockBuilder.append(value);
            return this;
        }
    }
}