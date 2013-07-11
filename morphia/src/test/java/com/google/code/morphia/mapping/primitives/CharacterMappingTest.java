package com.google.code.morphia.mapping.primitives;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import com.google.code.morphia.TestBase;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.mapping.MappingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;


/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class CharacterMappingTest extends TestBase {
    public static class Characters {
        @Id
        ObjectId id;
        List<Character[]> listWrapperArray = new ArrayList<Character[]>();
        List<char[]> listPrimitiveArray = new ArrayList<char[]>();
        List<Character> listWrapper = new ArrayList<Character>();
        char singlePrimitive;
        Character singleWrapper;
        char[] primitiveArray;
        Character[] wrapperArray;
        char[][] nestedPrimitiveArray;
        Character[][] nestedWrapperArray;
    }

    @Test
    public void mapping() throws Exception {
        morphia.map(Characters.class);
        final Characters entity = new Characters();
        entity.listWrapperArray.add(new Character[] {'1', 'g', '#'});
        entity.listPrimitiveArray.add(new char[] {'1', 'd', 'z'});
        entity.listWrapper.addAll(Arrays.asList('*', ' ', '\u8888'));
        entity.singlePrimitive = 'a';
        entity.singleWrapper = 'b';
        entity.primitiveArray = new char[] {'a', 'b'};
        entity.wrapperArray = new Character[] {'X', 'y', 'Z'};
        entity.nestedPrimitiveArray = new char[][] {{'5', '-'}, {'a', 'b'}};
        entity.nestedWrapperArray = new Character[][] {{'*', '$', '\u4824'}, {'X', 'y', 'Z'}};
        ds.save(entity);

        final Characters loaded = ds.get(entity);
        Assert.assertNotNull(loaded.id);
        Assert.assertArrayEquals(entity.listWrapperArray.get(0), loaded.listWrapperArray.get(0));
        Assert.assertArrayEquals(entity.listPrimitiveArray.get(0), loaded.listPrimitiveArray.get(0));
        Assert.assertEquals(entity.listWrapper, loaded.listWrapper);
        Assert.assertEquals(entity.singlePrimitive, loaded.singlePrimitive);
        Assert.assertEquals(entity.singleWrapper, loaded.singleWrapper);
        Assert.assertArrayEquals(entity.primitiveArray, loaded.primitiveArray);
        Assert.assertArrayEquals(entity.wrapperArray, loaded.wrapperArray);
        Assert.assertArrayEquals(entity.nestedPrimitiveArray, loaded.nestedPrimitiveArray);
        Assert.assertArrayEquals(entity.nestedWrapperArray, loaded.nestedWrapperArray);
    }

    @Test
    public void singleCharToPrimitiveArray() {
        final Characters characters = testMapping("primitiveArray", "a");
        Assert.assertArrayEquals("a".toCharArray(), characters.primitiveArray);
        ds.save(characters, WriteConcern.FSYNCED);
    }

    @Test
    public void singleCharToPrimitive() {
        final Characters characters = testMapping("singlePrimitive", "a");
        Assert.assertEquals('a', characters.singlePrimitive);
    }

    @Test
    public void singleCharToWrapperArray() {
        final Characters characters = testMapping("wrapperArray", "a");
        compare("a", characters.wrapperArray);
    }

    @Test
    public void singleCharToWrapper() {
        final Characters characters = testMapping("singleWrapper", "a");
        Assert.assertEquals(new Character('a'), characters.singleWrapper);
    }

    @Test(expected = MappingException.class)
    public void stringToPrimitive() {
        final Characters characters = testMapping("singlePrimitive", "ab");
    }

    @Test(expected = MappingException.class)
    public void stringToWrapper() {
        final Characters characters = testMapping("singleWrapper", "ab");
    }

    @Test
    public void stringToPrimitiveArray() {
        final Characters characters = testMapping("primitiveArray", "abc");
        Assert.assertArrayEquals("abc".toCharArray(), characters.primitiveArray);
    }

    @Test
    public void stringToWrapperArray() {
        final Characters characters = testMapping("wrapperArray", "abc");
        compare("abc", characters.wrapperArray);
    }

    @Test
    public void emptyStringToPrimitiveArray() {
        final Characters characters = testMapping("primitiveArray", "");
        Assert.assertArrayEquals("".toCharArray(), characters.primitiveArray);
    }

    @Test
    public void emptyStringToWrapperArray() {
        final Characters characters = testMapping("wrapperArray", "");
        compare("", characters.wrapperArray);
    }

    @Test
    public void emptyStringToPrimitive() {
        final Characters characters = testMapping("singlePrimitive", "");
        Assert.assertEquals(0, characters.singlePrimitive);
    }

    @Test
    public void emptyStringToWrapper() {
        final Characters characters = testMapping("singleWrapper", "");
        Assert.assertEquals(new Character((char) 0), characters.singleWrapper);
    }

    private Characters testMapping(final String field, final String value) {
        morphia.map(Characters.class);

        final DBCollection collection = ds.getCollection(Characters.class);
        collection.insert(new BasicDBObject(field, value));

        return ds.find(Characters.class).get();
    }

    private void compare(final String abc, final Character[] wrapperArray) {
        Assert.assertEquals(abc.length(), wrapperArray.length);
        for (int i = 0; i < wrapperArray.length; i++) {
            Assert.assertEquals(abc.charAt(i), wrapperArray[i].charValue());
        }
    }
}