package pt.pak3nuh.hollywood.processor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
/**
 * Marker annotation to generate the supporting infrastructure such as the actor proxy and the abstract factory.
 *
 * This annotation must only be used on interfaces because the annotation processor isn't prepared to override
 * methods of superclasses.
 *
 * The typical use case involves at least four distinct types
 * 1. An interface for the actor API
 * 2. One or many implementations for the actor API
 * 3. An actor factory to create instances of the actor implementations
 * 4. A proxy to work with the implementations
 *
 * Some of those types are generated for convenience or at least stubbed.
 *
 * It is possible to follow a different structure than this, but it may fail in unexpected ways. Each version
 * of the system may impose different restrictions that can break existing use cases.
 * If the above structure is followed, code shouldn't break just by bumping versions.
 * @param value The base **open** class of the actor proxy. It is highly recommended to extend [ActorProxyBase]
 * because the generated proxy classes may require some features defined there.
 *
 * **Custom proxies are currently considered experimental and may change in the future.**
 */
annotation class Actor(val value: KClass<*> = ActorProxyBase::class)
