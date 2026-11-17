# Glance draws the widgets from a WorkManager worker. WorkManager creates its workers and
# input mergers by class name, which R8 can't see, so their constructors must be kept: without
# these every widget stays on its loading spinner.
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
