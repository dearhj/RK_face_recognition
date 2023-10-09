## 使用说明
- 使用android studio编译该工程
- 部署文件
```
adb shell mkdir /sdcard/Android/data/iva
adb push model/rockiva_data_rk3588 /sdcard/Android/data/iva/
adb shell mv /sdcard/Android/data/iva/rockiva_data_rk3588 /sdcard/Android/data/iva/rockiva_data
adb shell chmod 0777 -R /sdcard/Android/data/iva/
```
- 授权
通过授权工具获取license授权后将key.lic放到`/sdcard/Android/data/iva/key.lic`路径，并且确定应用有读取文件的权限
