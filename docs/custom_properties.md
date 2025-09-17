# V1
Version V1 is based on reflection and will retrieve all methods and properties of the View class annotated with @ViewDebug.ExportedProperty for printing.
The sample code is shown as follows.
```kotlin
package yk.demo.view
import android.content.Context
import android.view.View
import android.view.ViewDebug
import android.view.ViewHierarchyEncoder

class CustomView(ctx: Context) : View(ctx) {

    @ViewDebug.ExportedProperty
    val v1Filed = "v1 filed"

    // Only non-void methods with no arguments
    @ViewDebug.ExportedProperty
    fun funv1(): String = "v1 function"
}
```

# V2
V2 needs to override a hidden API of View: fun encodeProperties(encoder: ViewHierarchyEncoder)
```kotlin
package yk.demo.view
import android.content.Context
import android.view.View
import android.view.ViewDebug
import android.view.ViewHierarchyEncoder

class CustomView(ctx: Context) : View(ctx) {
    // v2 need access hidden api
    override fun encodeProperties(encoder: ViewHierarchyEncoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("drawing:customV2", "CUSTOM V2");
    }
}
```