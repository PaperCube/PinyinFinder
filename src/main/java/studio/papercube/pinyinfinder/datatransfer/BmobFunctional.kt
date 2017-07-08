package studio.papercube.pinyinfinder.datatransfer

import cn.bmob.v3.BmobObject
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener

inline fun BmobObject.save(crossinline saveListener: (String?, BmobException?) -> Unit) {
    save(object : SaveListener<String>() {
        override fun done(p0: String?, p1: BmobException?) {
            saveListener(p0, p1)
        }
    })
}

inline fun <T> BmobQuery<T>.getObject(id: String, crossinline queryListener: (T?, BmobException?) -> Unit) {
    getObject(id, object : QueryListener<T>() {
        override fun done(p0: T, p1: BmobException?) {
            queryListener(p0, p1)
        }
    })
}

inline fun BmobObject.update(id: String, crossinline updateListener: (BmobException?) -> Unit) {
    update(id, object : UpdateListener() {
        override fun done(p0: BmobException?) {
            updateListener(p0)
        }
    })
}