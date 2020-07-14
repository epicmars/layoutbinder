LayoutBinder
================

[ ![Download](https://api.bintray.com/packages/jastrelax/maven/com.androidpi%3Alayoutbinder/images/download.svg) ](https://bintray.com/jastrelax/maven/com.androidpi%3Alayoutbinder/_latestVersion)

Bind layout resource to your activity or fragment with annotation.

## Dependencies

```groovy
dependencies {
    implementation 'com.androidpi:layoutbinder:1.3.0'
    annotationProcessor 'com.androidpi:layoutbinder-compiler:1.3.0'
}
```
As usual, to support kotlin, replace "annotationProcessor" with "kapt", and apply plugin "kotlin-kapt"
to your project's build script.

## Usage
### Quick start
```java

@BindLayout(R.layout.activity_example)
public class ExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutBinder.bind(this);
    }
}

@BindLayout(R.layout.fragment_example)
public class ExampleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutBinder.bind(this, inflater, container, false).getView();
    }
}

```

## How it works
Like Dagger and ButterKnife you might have used, it generate some boilerplate code
for you with an annotation processor.
