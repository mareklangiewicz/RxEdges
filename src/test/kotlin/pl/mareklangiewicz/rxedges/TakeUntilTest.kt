package pl.mareklangiewicz.rxedges

import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@RunWith(USpekRunner::class)
class TakeUntilTest {

    private val someError = RuntimeException("some rx error")

    @Test
    fun uspek() = uspek {
        "On takeUntil" o {
            val inputS = PublishSubject.create<Int>()
            val signalS = PublishSubject.create<Unit>()
            val outputS = inputS.takeUntil(signalS)
            val observer = outputS.test()

            "On start" o {
                "subscribe to input" o { assert(inputS.hasObservers()) }
                "subscribe to signal" o { assert(signalS.hasObservers()) }
                "do not emit anything" o { observer.assertEmpty() }

                "On signal" o {
                    signalS.onNext(Unit)

                    "no errors" o { observer.assertNoErrors() }
                    "complete" o { observer.assertComplete() }
                }
            }

            "On first input item" o {
                inputS.onNext(1)

                "emit first item" o { observer.assertValue(1) }
                "not complete" o { observer.assertNotComplete() }

                "On signal" o {
                    signalS.onNext(Unit)

                    "no errors" o { observer.assertNoErrors() }
                    "complete" o { observer.assertComplete() }
                    "unsubscribe from input" o { assert(!inputS.hasObservers()) }
                }

                "On input completion" o {
                    inputS.onComplete()

                    "complete" o { observer.assertComplete() }
                    "unsubscribe from signal" o { assert(!signalS.hasObservers()) }
                }

                "On input error" o {
                    inputS.onError(someError)

                    "emit error" o { observer.assertError(someError) }
                    "unsubscribe from signal" o { assert(!signalS.hasObservers()) }
                }

                "On second input item" o {
                    inputS.onNext(2)

                    "emit second item" o { observer.assertValues(1, 2) }

                    "On signal" o {
                        signalS.onNext(Unit)

                        "no new output" o { observer.assertValueCount(2) }
                        "no errors" o { observer.assertNoErrors() }
                        "complete" o { observer.assertComplete() }
                    }

                    "On signal completion" o {
                        signalS.onComplete()

                        "complete" o { observer.assertComplete() } // IMPORTANT EDGE CASE
                        "unsubscribe from input" o { assert(!inputS.hasObservers()) }
                    }

                    "On signal error" o {
                        signalS.onError(someError)

                        "emit the same error" o { observer.assertError(someError) }
                        "unsubscribe from input" o { assert(!inputS.hasObservers()) }
                    }
                }
            }
        }
    }
}