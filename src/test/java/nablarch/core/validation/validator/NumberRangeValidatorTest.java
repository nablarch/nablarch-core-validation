package nablarch.core.validation.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.convertor.TestTarget;
import nablarch.core.validation.creator.ReflectionFormCreator;

import org.junit.Before;
import org.junit.Test;


public class NumberRangeValidatorTest {

    private NumberRangeValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}は{2}以下で入力してください。", "EN", "{0} cannot be greater than {2}." },
        { "MSG00002", "ja", "{0}は{1}以上{2}以下で入力してください。", "EN", "{0} is not in the range {1} through {2}." },
        { "MSG00003", "ja", "{0}は{1}以上で入力してください。", "EN", "{0} cannot be lesser than {2}." },
        { "MSG00004", "ja", "テストメッセージ01", "EN", "test message 01" },
        { "PROP0001", "ja", "プロパティ1", "EN", "property1" }, };


    @Before
    public void setUpClass() {
        final XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("nablarch/core/validation/convertor-test-base.xml");
        final DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new NumberRangeValidator();
        testee.setMaxMessageId("MSG00001");
        testee.setMaxAndMinMessageId("MSG00002");
        testee.setMinMessageId("MSG00003");


        final Map<String, String[]> params = new HashMap<String, String[]>();
        
        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

    }

    private final NumberRange range = new NumberRange() {
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        @Override
        public long min() {
            return 10;
        }
        
        @Override
        public String messageId() {
            return "";
        }
        
        @Override
        public long max() {
            return 20;
        }
    };

    @Test
    public void testValidateSuccess() {

        
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10l));        
        assertTrue(testee.validate(context, "param", "PROP0001", range, 11l));       
        assertTrue(testee.validate(context, "param", "PROP0001", range, 20l));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10));
        assertTrue(testee.validate(context, "param", "PROP0001", range, new BigDecimal(11)));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10.1));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10.1f));
        assertTrue(testee.validate(context, "param", "PROP0001", range, null));
        
    }

    @Test
    public void testValidateGreaterWithMin() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinInteger() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinBigDecimal() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, new BigDecimal(21)));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinDouble() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 20.2));
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20以下で入力してください。", context.getMessages().get(0).formatMessage());

        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("max", 20L);
        }}, 20.2));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinFloat() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 20.2f));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }


    private final NumberRange lesserRange = new NumberRange() {
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        @Override
        public long min() {
            return 10;
        }
        
        @Override
        public String messageId() {
            return "";
        }
        
        @Override
        public long max() {
            return Long.MAX_VALUE;
        }
    };

    @Test
    public void testValidateLesser() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserInteger() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserBigDecimal() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, new BigDecimal(9l)));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserDouble() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9.9));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserFloat() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9.9f));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());


        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("min", 10L);
        }}, 9.9f));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateNull() {
    	// NULLはrequiredではない場合にありえる。

        assertTrue(testee.validate(context, "param", "PROP0001", lesserRange, null));    
    }

    @Test
    public void testValidateGreater() {
        final NumberRange range = new NumberRange() {
            
            @Override
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            @Override
            public long min() {
                return Long.MIN_VALUE;
            }
            
            @Override
            public String messageId() {
                return "";
            }
            
            @Override
            public long max() {
                return 20;
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    private final NumberRange range03 = new NumberRange() {
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        @Override
        public long min() {
            return 10;
        }
        
        @Override
        public String messageId() {
        	return "MSG00004";
        }
        
        @Override
        public long max() {
            return 20;
        }
    };

    @Test
    public void testValidateLesserAnnotationMessage() {

        assertFalse(testee.validate(context, "param", "PROP0001", range03, 9l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterAnnotationMessage() {
        final NumberRange range = new NumberRange() {
            
            @Override
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            @Override
            public long min() {
                return Long.MIN_VALUE;
            }
            
            @Override
            public String messageId() {
                return "MSG00004";
            }
            
            @Override
            public long max() {
                return 20;
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01", context.getMessages().get(0).formatMessage());

        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("min", 0L);
            put("max", 20L);
            put("messageId", "MSG00004");
        }}, 211));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateMaxNotSpecified() {
        final NumberRange range = new NumberRange() {
            
            @Override
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            @Override
            public long min() {
                return Long.MIN_VALUE;
            }
            
            @Override
            public String messageId() {
                return "MSG00004";
            }
            
            @Override
            public long max() {
                return Long.MAX_VALUE;
            }
        };

        assertTrue(testee.validate(context, "param", "PROP0001", range, 21l));    
    }

    @Test
    public void testGetAnnotationClass() {
    	assertEquals(NumberRange.class, testee.getAnnotationClass());
    }

}
