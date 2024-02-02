$(function () {
    nthTabs = $("#custom-id").nthTabs();
    passwordValidator();
    // setMainHeight()
})

/**
 * 创建普通Tab
 * @param title
 * @param url
 */
function addTab(id, title, url) {
    nthTabs.addTab({
        id: id,
        title: title,
        url: url
    });
}

function delTab(id) {
    nthTabs.delTab(id);
}

function toggleTab(id) {
    nthTabs.toggleTab(id);
}

function refreshTab(id) {
    var $iframe = $('#' + id).find('> iframe')

    if ($iframe.length > 0) {
        $iframe[0].contentWindow.location.reload(true);
    }
}

function showMessage(message) {
    toastr.success(message);
}

function setMainHeight() {
    $('#main').height($(window).height() - 55 - 50)
}

function passwordValidator() {
    $("#passwordForm").validate({
        rules: {
            newPassword2: {
                equalTo : "#newPassword"
            },
            password: {
                remote:{ // $.ajax
                    url: '/password/check',
                    type:'get',
                    data:{
                        password: function(){
                            return $("#password").val();
                        }
                    }
                }
            }
        },
        messages: {
            newPassword2: {
                equalTo : "两次密码不相同"
            },
            password: {
                remote:'旧密码不正确'
            }
        },
        submitHandler: function(form) {
            // form.submit()
            $.LoadingOverlay('show')
            axios.post('/password/update', {
                password: $("#newPassword").val()
            })
            .then(function (response) {
               form.reset();
               toastr.success('密码修改成功');
                $.LoadingOverlay('hide')
               $('#passwordModal').modal('hide');

            })
            .catch(function (error) {
                console.error(error);
                $.LoadingOverlay('hide')
            });

        }
    })
}