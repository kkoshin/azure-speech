import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingTest {
    @Test
    fun greet() {
        assertEquals(Greeting.greeting().isNotEmpty(), true)
    }
}