# SnapLite

تطبيق أندرويد (Kotlin) لتحميل الفيديو والصوت — فكرته قريبة من SnapTube.

## المميزات
- بحث في يوتيوب أو لصق رابط مباشر
- استقبال الروابط من زر "مشاركة" في أي تطبيق
- اختيار الجودة (فيديو بصوت / صوت فقط)
- تحميل عبر DownloadManager مع إشعارات → مجلد `Downloads/SnapLite`
- بناء تلقائي للـ APK عبر GitHub Actions

## البناء
1. ارفع المشروع على GitHub (فرع `main`).
2. من تبويب **Actions** شغّل **Build APK**.
3. نزّل الملف من **Artifacts** → `SnapLite-debug-apk`.

محلياً: افتح المشروع في Android Studio أو شغّل `gradle assembleDebug`.

## ملاحظات
- يعتمد على [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) وهو **GPLv3**، لذلك يجب أن يكون مشروعك GPLv3 أيضاً (أضف ملف LICENSE).
- استخدمه للمحتوى المسموح لك بتحميله فقط، والتزم بشروط المواقع وحقوق النشر.
- الـ APK الناتج debug؛ للنشر استخدم توقيع release.

## أفكار للتطوير
متصفح داخلي، تحويل MP3، قائمة تحميلات داخل التطبيق، دعم مواقع أخرى (SoundCloud/PeerTube متوفرة في المكتبة)، قوائم التشغيل، وضع داكن.
