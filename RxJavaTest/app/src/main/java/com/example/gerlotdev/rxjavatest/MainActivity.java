package com.example.gerlotdev.rxjavatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = MainActivity.class.getSimpleName();

	// Example classes
	class MyObject {

		private Long id;

		public MyObject(Long id) {
			this.id = id;
		}
	}

	class SomeResource {

		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public SomeResource(String url) {
			this.url = url;
		}
	}

	class MyApi {

		public MyObject getObjectWithId(Long l) {
			return new MyObject(l);
		}

		public Observable<SomeResource> getSomeResource(String url) {
			return Observable.just(new SomeResource(url));
		}
	}

	private MyApi myApi = new MyApi();

	Scheduler thread1 = new Scheduler() {
		@Override
		public Worker createWorker() {
			return null;
		}
	};

	Scheduler thread2 = new Scheduler() {
		@Override
		public Worker createWorker() {
			return null;
		}
	};

	// Creating a simple Subscriber
	Subscriber<String> mySubscriber = new Subscriber<String>() {
		@Override
		public void onNext(String s) {
			// This is where you can do something with
			// the incoming value from the observable
		}

		@Override
		public void onCompleted() {
			// This method is called when the observable
			// finished emitting events
		}

		@Override
		public void onError(Throwable e) {
			// This method is called when the observable encountered
			// an unrecoverable error
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		example1();
		example2();
		example3();
		example4();
		example5();
		example6();
		example7();
		example8();

	}

	private void showResourceOnUi(SomeResource res) {
		Log.d(TAG, "some resource with url: " + res.getUrl());
	}

	private void example1() {
		// Creating a simple Observable from a method
		// (which supposedly takes a long time to execute)
		// and subscribing to it
		// and storing the resulted subscription in a variable
		Subscription s = Observable.from(longOperation())
				.subscribe(mySubscriber);

		// Unsubscribe when no longer interested on the events
		// emitted by the observable
		s.unsubscribe();

		// Creating an Observable another way
		// The just method only emits one event
		// (one string in this example)
		// than completes
		Observable.just("Hello, world!");
	}

	private void example2() {
		// A typical hot observable that emits events every 10 seconds,
		// even without any subscribers
		Observable.interval(10, TimeUnit.SECONDS);

		// A cold observable that only emits events
		// when a Subscriber subscribes to it
		// The defer operator does not create an Observable
		// until someone subscribes to it
		// and creates a fresh observable for each Subscriber
		// You can wrap any Observables with the defer operator
		Observable<String> o = Observable.defer(new Func0<Observable<String>>() {
			@Override
			public Observable<String> call() {
				return Observable.just("Hello, world!");
			}
		});

		// The above observable will only emit events
		// when a Subscriber subscribes to it
		Subscription s = o.subscribe(mySubscriber);
	}

	private void example3() {
		// An Observable emitting a single number
		// and a map operator which does some kind of transformation on
		// the input, or return a completely different thing
		// here we return an object by calling an api with an id of it
		Observable.just(getMyObjectId())
				.map(new Func1<Long, MyObject>() {
					@Override
					public MyObject call(Long l) {
						return myApi.getObjectWithId(l);
					}
				})
				.subscribe(new Action1<MyObject>() {
					@Override
					public void call(MyObject o) {
						Log.d(TAG, "my object " + o.toString());
					}
				});
	}

	private void example4() {
		Observable.just("Hello, world!")
				.map(new Func1<String, Integer>() {
					@Override
					public Integer call(String s) {
						return potentialException(s);
					}
				})
				.map(new Func1<Integer, String>() {
					@Override
					public String call(Integer i) {
						return anotherPotentialException(i);
					}
				})
				.subscribe(new Subscriber<String>() {
					@Override
					public void onNext(String s) {
						Log.d(TAG, "next string: " + s);
					}

					@Override
					public void onCompleted() {
						Log.d(TAG, "completed");
					}

					@Override
					public void onError(Throwable e) {
						Log.e(TAG, "something went wrong");
					}
				});
	}

	private void example5() {
		Observable.from(longOperation())
				.subscribeOn(thread1)
				.observeOn(thread2)
				.subscribe(mySubscriber);
	}

	private void example6() {
		Observable.from(longOperation())
				.subscribeOn(thread1)
				.subscribe(mySubscriber);
		// Subscribing on thread1
		// Observing on thread1

		Observable.from(longOperation())
				.observeOn(thread2)
				.subscribe(mySubscriber);
	}

	private void example7() {
		String url = "http://example.url";
		myApi.getSomeResource(url)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<SomeResource>() {
					@Override
					public void call(SomeResource res) {
						showResourceOnUi(res);
					}
				});
	}

	private void example8() {
		// A simple RxJava example that prints Hello, world to LogCat
		Observable.just("Hello, world!")
				.subscribe(new Action1<String>() {
					@Override
					public void call(String s) {
						Log.d(TAG, s);
					}
				});
	}

	// Helper methods
	private String[] longOperation() {
		return new String[]{"Hello", "World"};
	}

	private Long getMyObjectId() {
		return 0L;
	}

	private Integer potentialException(String s) throws RuntimeException {
		throw new RuntimeException();
	}

	private String anotherPotentialException(Integer i) throws RuntimeException {
		return "DO NOTHING";
	}

}
