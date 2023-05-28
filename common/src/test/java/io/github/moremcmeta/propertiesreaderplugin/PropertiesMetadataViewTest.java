package io.github.moremcmeta.propertiesreaderplugin;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PropertiesMetadataView}.
 * @author soir20
 */
public class PropertiesMetadataViewTest {
    private static final InputStream MOCK_STREAM = new ByteArrayInputStream("stream".getBytes());

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullRoot_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new PropertiesMetadataView(null);
    }

    @Test
    public void size_Empty_0() {
        PropertiesMetadataView view = new PropertiesMetadataView(ImmutableMap.of());
        assertEquals(0, view.size());
    }

    @Test
    public void size_SingleLevelKeys_NumberOfKeys() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "hello", new PropertiesMetadataView.Value("10"),
                "world", new PropertiesMetadataView.Value("true"),
                "test", new PropertiesMetadataView.Value("good morning")
        );
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertEquals(3, view.size());
    }

    @Test
    public void size_MultiLevelKeys_NumberOfTopLevelKeys() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "hello", new PropertiesMetadataView.Value("10"),
                "world", new PropertiesMetadataView.Value("true"),
                "test", new PropertiesMetadataView.Value("good morning"),
                "testing", new PropertiesMetadataView.Value(ImmutableMap.of(
                        "metadata", new PropertiesMetadataView.Value("20"),
                        "view", new PropertiesMetadataView.Value("false")
                ))
        );
        
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertEquals(4, view.size());
    }
    
    @Test
    public void keys_Empty_NoKeys() {
        PropertiesMetadataView view = new PropertiesMetadataView(ImmutableMap.of());
        assertEquals(List.of(), collectKeys(view.keys()));
    }

    @Test
    public void keys_SingleLevelKeys_AllKeys() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "hello", new PropertiesMetadataView.Value("10"),
                "world", new PropertiesMetadataView.Value("true"),
                "test", new PropertiesMetadataView.Value("good morning")
        );
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertEquals(List.of("hello", "world", "test"), collectKeys(view.keys()));
    }

    @Test
    public void keys_MultiLevelKeys_TopLevelKeys() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "hello", new PropertiesMetadataView.Value("10"),
                "world", new PropertiesMetadataView.Value("true"),
                "test", new PropertiesMetadataView.Value("good morning"),
                "testing", new PropertiesMetadataView.Value(ImmutableMap.of(
                        "metadata", new PropertiesMetadataView.Value("20"),
                        "view", new PropertiesMetadataView.Value("false")
                ))
        );
        
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertEquals(List.of("hello", "world", "test", "testing"), collectKeys(view.keys()));
    }

    @Test
    public void hasKeyString_KeyNotPresent_False() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.hasKey("not present"));
    }

    @Test
    public void hasKeyString_KeyAtNextLevel_False() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.hasKey("valid subview val1"));
    }

    @Test
    public void hasKeyString_NullVal_False() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.hasKey("null val0"));
    }

    @Test
    public void hasKeyString_StringVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("string val0"));
    }

    @Test
    public void hasKeyString_PosIntVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("pos int val0"));
    }

    @Test
    public void hasKeyString_NegIntVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("neg int val0"));
    }

    @Test
    public void hasKeyString_PosLongVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("pos long val0"));
    }

    @Test
    public void hasKeyString_NegLongVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("neg long val0"));
    }

    @Test
    public void hasKeyString_PosBeyond64BitsVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("pos int >64-bits val0"));
    }

    @Test
    public void hasKeyString_NegBeyond64BitsVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("neg int >64-bits val0"));
    }

    @Test
    public void hasKeyString_PosFloatVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("pos float val0"));
    }

    @Test
    public void hasKeyString_NegFloatVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("neg float val0"));
    }

    @Test
    public void hasKeyString_PosDoubleVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("pos double val0"));
    }

    @Test
    public void hasKeyString_NegDoubleVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("neg double val0"));
    }

    @Test
    public void hasKeyString_TrueVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("true val0"));
    }

    @Test
    public void hasKeyString_FalseVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("false val0"));
    }

    @Test
    public void hasKeyString_ValidSubViewVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("valid subview val0"));
    }

    @Test
    public void hasKeyString_StreamVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey("stream val0"));
    }

    @Test
    public void hasKeyIndex_NegativeIndex_NegativeKeyIndexException() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        expectedException.expect(MetadataView.NegativeKeyIndexException.class);
        view.hasKey(-1);
    }

    @Test
    public void hasKeyIndex_StringVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(0));
    }

    @Test
    public void hasKeyIndex_PosIntVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(1));
    }

    @Test
    public void hasKeyIndex_NegIntVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(2));
    }

    @Test
    public void hasKeyIndex_PosLongVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(3));
    }

    @Test
    public void hasKeyIndex_NegLongVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(4));
    }

    @Test
    public void hasKeyIndex_PosBeyond64BitsVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(5));
    }

    @Test
    public void hasKeyIndex_NegBeyond64BitsVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(6));
    }

    @Test
    public void hasKeyIndex_PosFloatVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(7));
    }

    @Test
    public void hasKeyIndex_NegFloatVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(8));
    }

    @Test
    public void hasKeyIndex_PosDoubleVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(9));
    }

    @Test
    public void hasKeyIndex_NegDoubleVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(10));
    }

    @Test
    public void hasKeyIndex_TrueVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(11));
    }

    @Test
    public void hasKeyIndex_FalseVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(12));
    }

    @Test
    public void hasKeyIndex_ValidSubViewVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(13));
    }

    @Test
    public void hasKeyIndex_StreamVal_True() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.hasKey(14));
    }

    @Test
    public void hasKeyIndex_IndexTooLarge_False() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.hasKey(15));
    }

    @Test
    public void stringValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue("not present").isPresent());
    }

    @Test
    public void stringValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue("valid subview val1").isPresent());
    }

    @Test
    public void stringValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue("null val0").isPresent());
    }

    @Test
    public void stringValueString_StringVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("hello world", view.stringValue("string val0").orElseThrow());
    }

    @Test
    public void stringValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue("pos int val0").orElseThrow());
    }

    @Test
    public void stringValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue("neg int val0").orElseThrow());
    }

    @Test
    public void stringValueString_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue("pos long val0").orElseThrow());
    }

    @Test
    public void stringValueString_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue("neg long val0").orElseThrow());
    }

    @Test
    public void stringValueString_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("9223372036854775808", view.stringValue("pos int >64-bits val0").orElseThrow());
    }

    @Test
    public void stringValueString_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("-9223372036854775809", view.stringValue("neg int >64-bits val0").orElseThrow());
    }

    @Test
    public void stringValueString_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue("pos float val0").orElseThrow());
    }

    @Test
    public void stringValueString_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue("neg float val0").orElseThrow());
    }

    @Test
    public void stringValueString_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue("pos double val0").orElseThrow());
    }

    @Test
    public void stringValueString_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue("neg double val0").orElseThrow());
    }

    @Test
    public void stringValueString_TrueVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("true", view.stringValue("true val0").orElseThrow());
    }

    @Test
    public void stringValueString_FalseVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("false", view.stringValue("false val0").orElseThrow());
    }

    @Test
    public void stringValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue("valid subview val0").isPresent());
    }

    @Test
    public void stringValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue("stream val0").isPresent());
    }

    @Test
    public void stringValueIndex_StringVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("hello world", view.stringValue(0).orElseThrow());
    }

    @Test
    public void stringValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue(1).orElseThrow());
    }

    @Test
    public void stringValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue(2).orElseThrow());
    }

    @Test
    public void stringValueIndex_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue(3).orElseThrow());
    }

    @Test
    public void stringValueIndex_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue(4).orElseThrow());
    }

    @Test
    public void stringValueIndex_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("9223372036854775808", view.stringValue(5).orElseThrow());
    }

    @Test
    public void stringValueIndex_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("-9223372036854775809", view.stringValue(6).orElseThrow());
    }

    @Test
    public void stringValueIndex_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue(7).orElseThrow());
    }

    @Test
    public void stringValueIndex_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue(8).orElseThrow());
    }

    @Test
    public void stringValueIndex_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue(9).orElseThrow());
    }

    @Test
    public void stringValueIndex_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue(10).orElseThrow());
    }

    @Test
    public void stringValueIndex_TrueVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("true", view.stringValue(11).orElseThrow());
    }

    @Test
    public void stringValueIndex_FalseVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals("false", view.stringValue(12).orElseThrow());
    }

    @Test
    public void stringValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue(13).isPresent());
    }

    @Test
    public void stringValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue(14).isPresent());
    }

    @Test
    public void stringValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.stringValue(15).isPresent());
    }

    @Test
    public void integerValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("not present").isPresent());
    }

    @Test
    public void integerValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("valid subview val1").isPresent());
    }

    @Test
    public void integerValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("null val0").isPresent());
    }

    @Test
    public void integerValueString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("string val0").isPresent());
    }

    @Test
    public void integerValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue("pos int val0").orElseThrow());
    }

    @Test
    public void integerValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue("neg int val0").orElseThrow());
    }

    @Test
    public void integerValueString_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos long val0").isPresent());
    }

    @Test
    public void integerValueString_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("neg long val0").isPresent());
    }

    @Test
    public void integerValueString_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void integerValueString_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void integerValueString_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos float val0").isPresent());
    }

    @Test
    public void integerValueString_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("neg float val0").isPresent());
    }

    @Test
    public void integerValueString_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos double val0").isPresent());
    }

    @Test
    public void integerValueString_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("neg double val0").isPresent());
    }

    @Test
    public void integerValueString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("true val0").isPresent());
    }

    @Test
    public void integerValueString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("false val0").isPresent());
    }

    @Test
    public void integerValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("valid subview val0").isPresent());
    }

    @Test
    public void integerValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("stream val0").isPresent());
    }

    @Test
    public void integerValueIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(0).isPresent());
    }

    @Test
    public void integerValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue(1).orElseThrow());
    }

    @Test
    public void integerValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue(2).orElseThrow());
    }

    @Test
    public void integerValueIndex_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(3).isPresent());
    }

    @Test
    public void integerValueIndex_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(4).isPresent());
    }

    @Test
    public void integerValueIndex_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(5).isPresent());
    }

    @Test
    public void integerValueIndex_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(6).isPresent());
    }

    @Test
    public void integerValueIndex_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(7).isPresent());
    }

    @Test
    public void integerValueIndex_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(8).isPresent());
    }

    @Test
    public void integerValueIndex_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(9).isPresent());
    }

    @Test
    public void integerValueIndex_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(10).isPresent());
    }

    @Test
    public void integerValueIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(11).isPresent());
    }

    @Test
    public void integerValueIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(12).isPresent());
    }

    @Test
    public void integerValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(13).isPresent());
    }

    @Test
    public void integerValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(14).isPresent());
    }

    @Test
    public void integerValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue(15).isPresent());
    }

    @Test
    public void longValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("not present").isPresent());
    }

    @Test
    public void longValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("valid subview val1").isPresent());
    }

    @Test
    public void longValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("null val0").isPresent());
    }

    @Test
    public void longValueString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("string val0").isPresent());
    }

    @Test
    public void longValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, (long) view.longValue("pos int val0").orElseThrow());
    }

    @Test
    public void longValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, (long) view.longValue("neg int val0").orElseThrow());
    }

    @Test
    public void longValueString_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MAX_VALUE, (long) view.longValue("pos long val0").orElseThrow());
    }

    @Test
    public void longValueString_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MIN_VALUE, (long) view.longValue("neg long val0").orElseThrow());
    }

    @Test
    public void longValueString_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void longValueString_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void longValueString_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("pos float val0").isPresent());
    }

    @Test
    public void longValueString_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("neg float val0").isPresent());
    }

    @Test
    public void longValueString_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("pos double val0").isPresent());
    }

    @Test
    public void longValueString_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("neg double val0").isPresent());
    }

    @Test
    public void longValueString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("true val0").isPresent());
    }

    @Test
    public void longValueString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("false val0").isPresent());
    }

    @Test
    public void longValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("valid subview val0").isPresent());
    }

    @Test
    public void longValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue("stream val0").isPresent());
    }

    @Test
    public void longValueIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(0).isPresent());
    }

    @Test
    public void longValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, (long) view.longValue(1).orElseThrow());
    }

    @Test
    public void longValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, (long) view.longValue(2).orElseThrow());
    }

    @Test
    public void longValueIndex_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MAX_VALUE, (long) view.longValue(3).orElseThrow());
    }

    @Test
    public void longValueIndex_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MIN_VALUE, (long) view.longValue(4).orElseThrow());
    }

    @Test
    public void longValueIndex_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(5).isPresent());
    }

    @Test
    public void longValueIndex_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(6).isPresent());
    }

    @Test
    public void longValueIndex_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(7).isPresent());
    }

    @Test
    public void longValueIndex_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(8).isPresent());
    }

    @Test
    public void longValueIndex_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(9).isPresent());
    }

    @Test
    public void longValueIndex_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(10).isPresent());
    }

    @Test
    public void longValueIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(11).isPresent());
    }

    @Test
    public void longValueIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(12).isPresent());
    }

    @Test
    public void longValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(13).isPresent());
    }

    @Test
    public void longValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(14).isPresent());
    }

    @Test
    public void longValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.longValue(15).isPresent());
    }

    @Test
    public void floatValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("not present").isPresent());
    }

    @Test
    public void floatValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("valid subview val1").isPresent());
    }

    @Test
    public void floatValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("null val0").isPresent());
    }

    @Test
    public void floatValueString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("string val0").isPresent());
    }

    @Test
    public void floatValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Integer.MAX_VALUE, view.floatValue("pos int val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Integer.MIN_VALUE, view.floatValue("neg int val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Long.MAX_VALUE, view.floatValue("pos long val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Long.MIN_VALUE, view.floatValue("neg long val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(9223372036854775808f, view.floatValue("pos int >64-bits val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-9223372036854775809f, view.floatValue("neg int >64-bits val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Float.MAX_VALUE, view.floatValue("pos float val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-Float.MAX_VALUE, view.floatValue("neg float val0").orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueString_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("pos double val0").isPresent());
    }

    @Test
    public void floatValueString_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("neg double val0").isPresent());
    }

    @Test
    public void floatValueString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("true val0").isPresent());
    }

    @Test
    public void floatValueString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("false val0").isPresent());
    }

    @Test
    public void floatValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("valid subview val0").isPresent());
    }

    @Test
    public void floatValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue("stream val0").isPresent());
    }

    @Test
    public void floatValueIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(0).isPresent());
    }

    @Test
    public void floatValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Integer.MAX_VALUE, view.floatValue(1).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Integer.MIN_VALUE, view.floatValue(2).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Long.MAX_VALUE, view.floatValue(3).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals((float) Long.MIN_VALUE, view.floatValue(4).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(9223372036854775808f, view.floatValue(5).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-9223372036854775809f, view.floatValue(6).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Float.MAX_VALUE, view.floatValue(7).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-Float.MAX_VALUE, view.floatValue(8).orElseThrow(), 0.000001);
    }

    @Test
    public void floatValueIndex_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(9).isPresent());
    }

    @Test
    public void floatValueIndex_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(10).isPresent());
    }

    @Test
    public void floatValueIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(11).isPresent());
    }

    @Test
    public void floatValueIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(12).isPresent());
    }

    @Test
    public void floatValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(13).isPresent());
    }

    @Test
    public void floatValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(14).isPresent());
    }

    @Test
    public void floatValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.floatValue(15).isPresent());
    }

    @Test
    public void doubleValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("not present").isPresent());
    }

    @Test
    public void doubleValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("valid subview val1").isPresent());
    }

    @Test
    public void doubleValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("null val0").isPresent());
    }

    @Test
    public void doubleValueString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("string val0").isPresent());
    }

    @Test
    public void doubleValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, view.doubleValue("pos int val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, view.doubleValue("neg int val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MAX_VALUE, view.doubleValue("pos long val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MIN_VALUE, view.doubleValue("neg long val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(9223372036854775808d, view.doubleValue("pos int >64-bits val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-9223372036854775809d, view.doubleValue("neg int >64-bits val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(3.4028235E38d, view.doubleValue("pos float val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-3.4028235E38d, view.doubleValue("neg float val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Double.MAX_VALUE, view.doubleValue("pos double val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-Double.MAX_VALUE, view.doubleValue("neg double val0").orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("true val0").isPresent());
    }

    @Test
    public void doubleValueString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("false val0").isPresent());
    }

    @Test
    public void doubleValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("valid subview val0").isPresent());
    }

    @Test
    public void doubleValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue("stream val0").isPresent());
    }

    @Test
    public void doubleValueIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(0).isPresent());
    }

    @Test
    public void doubleValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, view.doubleValue(1).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, view.doubleValue(2).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MAX_VALUE, view.doubleValue(3).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Long.MIN_VALUE, view.doubleValue(4).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(9223372036854775808d, view.doubleValue(5).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-9223372036854775809d, view.doubleValue(6).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(3.4028235E38d, view.doubleValue(7).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-3.4028235E38d, view.doubleValue(8).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Double.MAX_VALUE, view.doubleValue(9).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(-Double.MAX_VALUE, view.doubleValue(10).orElseThrow(), 0.000001);
    }

    @Test
    public void doubleValueIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(11).isPresent());
    }

    @Test
    public void doubleValueIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(12).isPresent());
    }

    @Test
    public void doubleValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(13).isPresent());
    }

    @Test
    public void doubleValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(14).isPresent());
    }

    @Test
    public void doubleValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.doubleValue(15).isPresent());
    }

    @Test
    public void booleanValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("not present").isPresent());
    }

    @Test
    public void booleanValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("valid subview val1").isPresent());
    }

    @Test
    public void booleanValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("null val0").isPresent());
    }

    @Test
    public void booleanValueString_StringVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("string val0").orElseThrow());
    }

    @Test
    public void booleanValueString_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("pos int val0").orElseThrow());
    }

    @Test
    public void booleanValueString_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("neg int val0").orElseThrow());
    }

    @Test
    public void booleanValueString_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("pos long val0").orElseThrow());
    }

    @Test
    public void booleanValueString_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("neg long val0").orElseThrow());
    }

    @Test
    public void booleanValueString_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("pos int >64-bits val0").orElseThrow());
    }

    @Test
    public void booleanValueString_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("neg int >64-bits val0").orElseThrow());
    }

    @Test
    public void booleanValueString_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("pos float val0").orElseThrow());
    }

    @Test
    public void booleanValueString_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("neg float val0").orElseThrow());
    }

    @Test
    public void booleanValueString_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("pos double val0").orElseThrow());
    }

    @Test
    public void booleanValueString_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("neg double val0").orElseThrow());
    }

    @Test
    public void booleanValueString_TrueVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.booleanValue("true val0").orElseThrow());
    }

    @Test
    public void booleanValueString_FalseVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("false val0").orElseThrow());
    }

    @Test
    public void booleanValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("valid subview val0").isPresent());
    }

    @Test
    public void booleanValueString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue("stream val0").isPresent());
    }

    @Test
    public void booleanValueIndex_StringVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(0).orElseThrow());
    }

    @Test
    public void booleanValueIndex_PosIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(1).orElseThrow());
    }

    @Test
    public void booleanValueIndex_NegIntVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(2).orElseThrow());
    }

    @Test
    public void booleanValueIndex_PosLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(3).orElseThrow());
    }

    @Test
    public void booleanValueIndex_NegLongVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(4).orElseThrow());
    }

    @Test
    public void booleanValueIndex_PosBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(5).orElseThrow());
    }

    @Test
    public void booleanValueIndex_NegBeyond64BitsVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(6).orElseThrow());
    }

    @Test
    public void booleanValueIndex_PosFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(7).orElseThrow());
    }

    @Test
    public void booleanValueIndex_NegFloatVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(8).orElseThrow());
    }

    @Test
    public void booleanValueIndex_PosDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(9).orElseThrow());
    }

    @Test
    public void booleanValueIndex_NegDoubleVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(10).orElseThrow());
    }

    @Test
    public void booleanValueIndex_TrueVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertTrue(view.booleanValue(11).orElseThrow());
    }

    @Test
    public void booleanValueIndex_FalseVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(12).orElseThrow());
    }

    @Test
    public void booleanValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(13).isPresent());
    }

    @Test
    public void booleanValueString_TrueString_CaseInsensitive() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "lowercase", new PropertiesMetadataView.Value("true"),
                "uppercase", new PropertiesMetadataView.Value("TRUE"),
                "mixed case", new PropertiesMetadataView.Value("TrUe")
        );
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertTrue(view.booleanValue("lowercase").orElseThrow());
        assertTrue(view.booleanValue("uppercase").orElseThrow());
        assertTrue(view.booleanValue("mixed case").orElseThrow());
    }

    @Test
    public void booleanValueIndex_TrueString_CaseInsensitive() {
        ImmutableMap<String, PropertiesMetadataView.Value> root = ImmutableMap.of(
                "lowercase", new PropertiesMetadataView.Value("true"),
                "uppercase", new PropertiesMetadataView.Value("TRUE"),
                "mixed case", new PropertiesMetadataView.Value("TrUe")
        );
        PropertiesMetadataView view = new PropertiesMetadataView(root);
        assertTrue(view.booleanValue(0).orElseThrow());
        assertTrue(view.booleanValue(2).orElseThrow());
        assertTrue(view.booleanValue(1).orElseThrow());
    }

    @Test
    public void booleanValueIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(14).isPresent());
    }

    @Test
    public void booleanValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.booleanValue(15).isPresent());
    }

    @Test
    public void subViewString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("not present").isPresent());
    }

    @Test
    public void subViewString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("valid subview val1").isPresent());
    }

    @Test
    public void subViewString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("null val0").isPresent());
    }

    @Test
    public void subViewString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("string val0").isPresent());
    }

    @Test
    public void subViewString_PosIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("pos int val0").isPresent());
    }

    @Test
    public void subViewString_NegIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("neg int val0").isPresent());
    }

    @Test
    public void subViewString_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("pos long val0").isPresent());
    }

    @Test
    public void subViewString_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("neg long val0").isPresent());
    }

    @Test
    public void subViewString_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("pos int >64-bits val0").isPresent());
    }

    @Test
    public void subViewString_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("neg int >64-bits val0").isPresent());
    }

    @Test
    public void subViewString_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("pos float val0").isPresent());
    }

    @Test
    public void subViewString_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("neg float val0").isPresent());
    }

    @Test
    public void subViewString_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("pos double val0").isPresent());
    }

    @Test
    public void subViewString_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("neg double val0").isPresent());
    }

    @Test
    public void subViewString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("true val0").isPresent());
    }

    @Test
    public void subViewString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("false val0").isPresent());
    }

    @Test
    public void subViewString_ValidSubViewVal_ViewFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(
                Set.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "valid subview val1",
                        "stream val1"),
                new HashSet<>(collectKeys(view.subView("valid subview val0").orElseThrow().keys()))
        );
    }

    @Test
    public void subViewString_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView("stream val0").isPresent());
    }

    @Test
    public void subViewIndex_NegativeIndex_NegativeKeyIndexException() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        expectedException.expect(MetadataView.NegativeKeyIndexException.class);
        assertFalse(view.subView(-1).isPresent());
    }

    @Test
    public void subViewIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(0).isPresent());
    }

    @Test
    public void subViewIndex_PosIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(1).isPresent());
    }

    @Test
    public void subViewIndex_NegIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(2).isPresent());
    }

    @Test
    public void subViewIndex_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(3).isPresent());
    }

    @Test
    public void subViewIndex_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(4).isPresent());
    }

    @Test
    public void subViewIndex_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(5).isPresent());
    }

    @Test
    public void subViewIndex_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(6).isPresent());
    }

    @Test
    public void subViewIndex_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(7).isPresent());
    }

    @Test
    public void subViewIndex_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(8).isPresent());
    }

    @Test
    public void subViewIndex_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(9).isPresent());
    }

    @Test
    public void subViewIndex_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(10).isPresent());
    }

    @Test
    public void subViewIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(11).isPresent());
    }

    @Test
    public void subViewIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(12).isPresent());
    }

    @Test
    public void subViewIndex_ValidSubViewVal_ViewFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(
                Set.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "valid subview val1",
                        "stream val1"),
                new HashSet<>(collectKeys(view.subView(13).orElseThrow().keys()))
        );
    }

    @Test
    public void subViewIndex_StreamVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(14).isPresent());
    }

    @Test
    public void subViewIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.subView(15).isPresent());
    }

    @Test
    public void streamValueString_KeyNotPresent_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("not present").isPresent());
    }

    @Test
    public void streamValueString_KeyAtNextLevel_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("valid subview val1").isPresent());
    }

    @Test
    public void streamValueString_NullVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("null val0").isPresent());
    }

    @Test
    public void streamValueString_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("string val0").isPresent());
    }

    @Test
    public void streamValueString_PosIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue("pos int val0").orElseThrow());
    }

    @Test
    public void streamValueString_NegIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue("neg int val0").orElseThrow());
    }

    @Test
    public void streamValueString_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos long val0").isPresent());
    }

    @Test
    public void streamValueString_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("neg long val0").isPresent());
    }

    @Test
    public void streamValueString_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.integerValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void streamValueString_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void streamValueString_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("pos float val0").isPresent());
    }

    @Test
    public void streamValueString_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("neg float val0").isPresent());
    }

    @Test
    public void streamValueString_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("pos double val0").isPresent());
    }

    @Test
    public void streamValueString_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("neg double val0").isPresent());
    }

    @Test
    public void streamValueString_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("true val0").isPresent());
    }

    @Test
    public void streamValueString_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("false val0").isPresent());
    }

    @Test
    public void streamValueString_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue("valid subview val0").isPresent());
    }

    @Test
    public void streamValueString_StreamVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(MOCK_STREAM, view.byteStreamValue("stream val0").orElseThrow());
    }

    @Test
    public void streamValueIndex_StringVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(0).isPresent());
    }

    @Test
    public void streamValueIndex_PosIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(1).isPresent());
    }

    @Test
    public void streamValueIndex_NegIntVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(2).isPresent());
    }

    @Test
    public void streamValueIndex_PosLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(3).isPresent());
    }

    @Test
    public void streamValueIndex_NegLongVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(4).isPresent());
    }

    @Test
    public void streamValueIndex_PosBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(5).isPresent());
    }

    @Test
    public void streamValueIndex_NegBeyond64BitsVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(6).isPresent());
    }

    @Test
    public void streamValueIndex_PosFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(7).isPresent());
    }

    @Test
    public void streamValueIndex_NegFloatVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(8).isPresent());
    }

    @Test
    public void streamValueIndex_PosDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(9).isPresent());
    }

    @Test
    public void streamValueIndex_NegDoubleVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(10).isPresent());
    }

    @Test
    public void streamValueIndex_TrueVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(11).isPresent());
    }

    @Test
    public void streamValueIndex_FalseVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(12).isPresent());
    }

    @Test
    public void streamValueIndex_ValidSubViewVal_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(13).isPresent());
    }

    @Test
    public void streamValueIndex_StreamVal_ValueFound() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertEquals(MOCK_STREAM, view.byteStreamValue(14).orElseThrow());
    }

    @Test
    public void streamValueIndex_IndexTooLarge_Empty() {
        PropertiesMetadataView view = new PropertiesMetadataView(makeDemoMap());
        assertFalse(view.byteStreamValue(15).isPresent());
    }

    private ImmutableMap<String, PropertiesMetadataView.Value> makeDemoMap() {
        return addAllTypeVals(
                addAllTypeVals(ImmutableMap.of(), 1),
                0
        );
    }

    private ImmutableMap<String, PropertiesMetadataView.Value> addAllTypeVals(
            ImmutableMap<String, PropertiesMetadataView.Value> subView,
            int level
    ) {
        ImmutableMap.Builder<String, PropertiesMetadataView.Value> root = new ImmutableMap.Builder<>();

        root.put("string val" + level, new PropertiesMetadataView.Value("hello world"));
        root.put("pos int val" + level, new PropertiesMetadataView.Value(String.valueOf(Integer.MAX_VALUE)));
        root.put("neg int val" + level, new PropertiesMetadataView.Value(String.valueOf(Integer.MIN_VALUE)));
        root.put("pos long val" + level, new PropertiesMetadataView.Value(String.valueOf(Long.MAX_VALUE)));
        root.put("neg long val" + level, new PropertiesMetadataView.Value(String.valueOf(Long.MIN_VALUE)));
        root.put("pos int >64-bits val" + level, new PropertiesMetadataView.Value(new BigInteger("9223372036854775808").toString()));
        root.put("neg int >64-bits val" + level, new PropertiesMetadataView.Value(new BigInteger("-9223372036854775809").toString()));
        root.put("pos float val" + level, new PropertiesMetadataView.Value(String.valueOf(Float.MAX_VALUE)));
        root.put("neg float val" + level, new PropertiesMetadataView.Value(String.valueOf(-Float.MAX_VALUE)));
        root.put("pos double val" + level, new PropertiesMetadataView.Value(String.valueOf(Double.MAX_VALUE)));
        root.put("neg double val" + level, new PropertiesMetadataView.Value(String.valueOf(-Double.MAX_VALUE)));
        root.put("true val" + level, new PropertiesMetadataView.Value(String.valueOf(true)));
        root.put("false val" + level, new PropertiesMetadataView.Value(String.valueOf(false)));
        root.put("valid subview val" + level, new PropertiesMetadataView.Value(subView));
        root.put("stream val" + level, new PropertiesMetadataView.Value(MOCK_STREAM));

        return root.build();
    }

    private List<String> collectKeys(Iterable<String> keys) {
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            keyList.add(key);
        }
        return keyList;
    }

}