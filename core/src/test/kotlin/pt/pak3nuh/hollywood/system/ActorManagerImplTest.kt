package pt.pak3nuh.hollywood.system

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNotSameAs
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

internal class ActorManagerImplTest {

    private val greeterFactory = GreeterFactory()
    private val dogFactory = DogFactory()
    private val repo = simpleRepository(greeterFactory, dogFactory)
    private val manager = ActorManagerImpl(repo)

    @Test
    internal fun `should always create actor`() {
        val actor1 = manager.createActor(GreeterFactory::class) { it.createGreeter() }
        val actor2 = manager.createActor(GreeterFactory::class) { it.createGreeter() }

        assertThat(actor1).isNotSameAs(actor2)
        assertThat(greeterFactory.createdProxies).isEqualTo(2)
        assertThat(greeterFactory.createdActors).isEqualTo(2)
    }

    @Test
    internal fun `should get existing actor`() {
        val actor1 = manager.getOrCreateActor("id", GreeterFactory::class) { it.createGreeter() }
        val actor2 = manager.getOrCreateActor("id", GreeterFactory::class) { it.createGreeter() }

        assertThat(actor1).isSameAs(actor2)
        assertThat(greeterFactory.createdProxies).isEqualTo(1)
        assertThat(greeterFactory.createdActors).isEqualTo(1)
    }

    @Test
    internal fun `should dispose actor`() {
        val actor1 = manager.getOrCreateActor("id", GreeterFactory::class) { it.createGreeter() }

        assertThat(manager.getActor("id", Greeter::class)).isNotNull()

        manager.disposeActor(actor1)

        assertThat(manager.getActor("id", Greeter::class)).isNull()
    }

    @Test
    internal fun `should allow same id for different actor type`() {
        val id = "some id"
        val greeter = manager.getOrCreateActor(id, GreeterFactory::class) { it.createGreeter() }
        val dog = manager.getOrCreateActor(id, DogFactory::class) { it.createDog() }

        assertThat(manager.getActor(id, Greeter::class)).isSameAs(greeter)
        assertThat(manager.getActor(id, Dog::class)).isSameAs(dog)
    }

    @Test
    internal fun `should return null if the actor type is not correct`() {
        val id = "some id"
        manager.getOrCreateActor(id, DogFactory::class) { it.createDog() }
        assertThat(manager.getActor(id, Greeter::class)).isNull()
    }

    @Test
    internal fun `should only execute creator function once`() {
        val future1 = CompletableFuture<Greeter>()
        val future2 = CompletableFuture<Greeter>()

        // tries to create 2 actor with the same id concurrently
        // the actor creation function must only be executed once since one of the request wins
        createActorInAnotherThread(future1)
        createActorInAnotherThread(future2)
        val actor1 = future1.join()
        val actor2 = future1.join()

        assertThat(actor1).isSameAs(actor2)
        assertThat(greeterFactory.createdActors).isEqualTo(1)
    }

    private fun createActorInAnotherThread(future: CompletableFuture<Greeter>) {
        thread {
            try {
                val actor = manager.getOrCreateActor("id", GreeterFactory::class) {
                    Thread.sleep(1000)
                    it.createGreeter()
                }
                future.complete(actor)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
    }

    @Test
    internal fun `should not allow dispose of unknown objects`() {
        assertThrows<IllegalArgumentException> {
            manager.disposeActor(Any())
        }
    }

    @Test
    internal fun `should allow multiple disposes of the same actor`() {
        val actor1 = manager.getOrCreateActor("id", GreeterFactory::class) { it.createGreeter() }

        manager.disposeActor(actor1)
        manager.disposeActor(actor1)
        manager.disposeActor(actor1)
        manager.disposeActor(actor1)
    }
}
