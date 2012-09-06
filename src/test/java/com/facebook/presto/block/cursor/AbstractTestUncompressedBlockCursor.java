/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.block.cursor;

import com.facebook.presto.Tuples;
import com.facebook.presto.block.cursor.BlockCursor;
import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public abstract class AbstractTestUncompressedBlockCursor
{
    @Test
    public void testStates()
            throws Exception
    {
        BlockCursor cursor = createCursor();

        //
        // We are before the first position, so all get current methods should throw an IllegalStateException
        //
        try {
            cursor.getTuple();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
        }

        try {
            cursor.tupleEquals(Tuples.createTuple(0L));
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
        }

        try {
            cursor.advanceNextValuePosition();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException expected) {
        }

        //
        // advance to end
        //
        while (cursor.hasNextValue()) {
            cursor.advanceNextValue();
        }
        while (cursor.hasNextValuePosition()) {
            cursor.advanceNextValuePosition();
        }

        //
        // We are at the last position, so all get next methods should throw a NoSuchElementException
        //

        try {
            cursor.advanceNextValue();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException expected) {
        }

        try {
            cursor.advanceNextValuePosition();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testAdvanceNextPosition()
    {
        BlockCursor cursor = createCursor();

        cursor.advanceNextValue();
        assertFalse(cursor.hasNextValuePosition());
        assertTrue(cursor.hasNextValue());

        cursor.advanceNextValue();
        assertFalse(cursor.hasNextValuePosition());
        assertTrue(cursor.hasNextValue());
    }

    protected abstract BlockCursor createCursor();
}