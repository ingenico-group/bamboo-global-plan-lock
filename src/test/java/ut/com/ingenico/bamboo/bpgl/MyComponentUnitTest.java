package ut.com.ingenico.bamboo.bpgl;

import org.junit.Test;
import com.ingenico.bamboo.bpgl.api.MyPluginComponent;
import com.ingenico.bamboo.bpgl.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}