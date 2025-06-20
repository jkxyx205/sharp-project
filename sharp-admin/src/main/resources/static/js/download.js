/**
 * 异步下载文件
 * @param path
 */
function download(path, completedCallback, failCallback) {
    const xhr = new XMLHttpRequest()
    xhr.onload = function () {
        if (this.response.type === 'application/json') {
            const fr = new FileReader();
            fr.readAsText(this.response)
            fr.onload = function (e) {
                let response = JSON.parse(e.target.result)
                const message = response.message ? response.message : response.error
                failCallback ? failCallback(message) : alert(message)
            }
        } else {
            let downloadElement = document.createElement('a');
            let href = window.URL.createObjectURL(this.response); //创建下载的链接
            downloadElement.href = href;
            downloadElement.download = decodeURIComponent(xhr.getResponseHeader("filename")); //下载后文件名
            document.body.appendChild(downloadElement);
            downloadElement.click(); //点击下载
            document.body.removeChild(downloadElement); //下载完成移除元素
            window.URL.revokeObjectURL(href);
        }
        completedCallback && completedCallback()
    }
    xhr.open('GET', path)
    xhr.responseType = 'blob'
    xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest")
    xhr.send()
}