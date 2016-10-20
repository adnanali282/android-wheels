# Android Wheels
Some useful tools for Android

### Contents
* ThreadUtils - Tools for asynchronous tasks in Android
* ImageLoader - Universal image loading tool
* HttpRequest - API for HTTP requests based on HttpURLConnection
* AsyncLoader - Abstract Loader based on ThreadUtils
* IterableQuery - Object query API
* CollectionUtils - Tools for collections
* CommonUtils - Common useful tools
* HashUtils - Tools for hashing
* SnackbarUtils - Tools for making Snackbar from Fragment, Activity, Window or View
* CsvParser - Parser/encoder of CSV format
* CircularProgressBar - Progress bar view, supports indeterminate mode
* and more...

### Usage
```
dependencies {
    compile 'com.budiyev.android:android-wheels:4.1.0'
}
```

### Examples
* ImageLoader
```
/**
 * Simple singleton {@link Uri} {@link ImageLoader} example
 */
public final class UriImageLoader extends ImageLoader<Uri> {
    private UriImageLoader(@NonNull Context context) {
        super(context);
        setBitmapLoader(newUriBitmapLoader());
        setMemoryImageCache(newMemoryImageCache());
        setStorageImageCache(newStorageImageCache(context));
    }

    public void loadImage(@NonNull Uri uri, @NonNull ImageView imageView) {
        loadImage(newImageSource(uri), imageView);
    }

    @SuppressLint("StaticFieldLeak")
    private static volatile Context sContext;

    /**
     * Initialize context for this loader
     * <br>
     * Use <b>application context</b> to avoid memory leaks.
     *
     * @param context context
     * @see Context#getApplicationContext()
     */
    public static void initialize(@NonNull Context context) {
        sContext = Objects.requireNonNull(context);
    }

    /**
     * Obtain an instance of this loader
     */
    @NonNull
    public static UriImageLoader getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class InstanceHolder {
        @SuppressLint("StaticFieldLeak")
        public static final UriImageLoader INSTANCE = new UriImageLoader(sContext);
    }
}
```

* HttpRequest
```
// Common simple cases

InputStream dataStream =
        HttpRequest.newGetRequest("http://www.google.ru/").execute().getDataStream();

String dataStringUtf16 = HttpRequest.newGetRequest("http://www.google.ru/").execute()
        .getDataString("UTF-16");

String dataStringDefault = // UTF-8
        HttpRequest.newGetRequest("http://www.google.ru/").execute().getDataString();

HttpRequestResult requestResult =
        HttpRequest.newGetRequest("http://www.google.ru/").execute();

Future<HttpRequestResult> futureResult =
        HttpRequest.newGetRequest("http://www.google.ru/").submit();

// More complex case

HttpRequest.newGetBuilder("http://www.google.ru/").addCallback(new HttpRequestCallback() {
    @Override
    public void onResult(@NonNull HttpRequestResult requestResult) {
    switch (requestResult.getResultType()) {
        case HttpRequestResult.ERROR_HTTP:
            // Do something
            break;
        case HttpRequestResult.ERROR_IO:
            // Do something
            break;
        case HttpRequestResult.ERROR_MALFORMED_URL:
            // Do something
            break;
        case HttpRequestResult.ERROR_PROTOCOL:
            // Do something
            break;
        case HttpRequestResult.ERROR_UNEXPECTED:
            // Do something
            break;
        case HttpRequestResult.ERROR_UNSUPPORTED_ENCODING:
            // Do something
            break;
        case HttpRequestResult.NONE:
            // Do something
            break;
        case HttpRequestResult.SUCCESS:
             // Do something
             break;
        }
    }
}).submit();
```

* AsyncLoader
```
/**
 * Simple URL picture loader
 */
public class PictureLoader extends AsyncLoader<Bundle, Bitmap> {
    public static final String EXTRA_PICTURE_URL = "extra_picture_url";

    public PictureLoader(@NonNull Context context, @Nullable Bundle arguments) {
        super(context, arguments);
    }

    @Nullable
    @Override
    protected Bitmap load(@Nullable Bundle arguments, @NonNull LoadState loadState) {
        if (arguments == null || !arguments.containsKey(EXTRA_PICTURE_URL)) {
            return null;
        }
        Bitmap bitmap = ImageLoader.loadSampledBitmapFromUri(getContext(),
                Uri.parse(arguments.getString(EXTRA_PICTURE_URL)), Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        // To avoid reloading, if activity was stopped or smth;
        // Read documentation for details
        loadState.setForceLoaded(true);
        return bitmap;
    }
}
```

* IterableQuery
```
// Reverse string

String reversed =
        IterableQuery.from("The quick brown fox jumps over the lazy dog".split(" "))
                .aggregate(new Aggregator<String, String>() {
                    @Override
                        public String apply(String accumulator, String value) {
                            return value + " " + accumulator;
                        }
                });

// reversed = "dog lazy the over jumps fox brown quick The"

//Get sum of 10 maximum salaries of employees

public double getTopTenSalary() {
    // Fill test data
    int personCount = 1000;
    Random random = new Random();
    List<Employee> employees = new ArrayList<>(personCount);
    for (int i = 0; i < personCount; i++) {
        employees.add(new Employee("Person " + (i + 1), random.nextDouble() * 10000));
    }
    // Get salary
    return IterableQuery.from(employees).sort(new Comparator<Employee>() {
        @Override
        public int compare(Employee o1, Employee o2) {
            double salary1 = o1.getSalary();
            double salary2 = o2.getSalary();
            return salary1 > salary2 ? -1 : salary1 == salary2 ? 0 : 1;
        }
    }).take(10).aggregate(0D, new Aggregator<Double, Employee>() {
        @Override
        public Double apply(Double accumulator, Employee value) {
            return accumulator + value.getSalary();
        }
    });
}

public class Employee {
    private String mName;
    private double mSalary;

    public Employee(String name, double salary) {
        mName = name;
        mSalary = salary;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getSalary() {
        return mSalary;
    }

    public void setSalary(double salary) {
        mSalary = salary;
    }
}
```

### Download
[![Download](https://api.bintray.com/packages/yuriy-budiyev/maven/android-wheels/images/download.svg)]
(https://bintray.com/yuriy-budiyev/maven/android-wheels/_latestVersion)
