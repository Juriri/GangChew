// common-ajax.js


export class jwtToken {
    static getUserToken() {
        return new Promise((resolve, reject) => {
            $.ajax({
                type: 'GET',
                cache: false,
                url: "/login/cookie",
                dataType: "json",
                success: function (data) {
                    var isSuccess = data.isSuccess;
                    if (isSuccess === true) {
                        resolve(data.result); // Resolve with the token
                    } else {
                        resolve(''); // Resolve with an empty token
                    }
                },
                error: function (error) {
                    reject("JWT request failed: " + error);
                }
            });
        });
    }
}

// 펀딩 디테일 객체 혹은 학생 게시글 디테일 객체를 불러오는 함수 (GET)
export function loadDetailObj(url) {
    return jwtToken.getUserToken() // Get the user token
        .then(jwtToken => {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "GET",
                    url: url,
                    headers: {
                        "Authorization": "Bearer " + jwtToken
                    },
                    success: function (data) {
                        resolve(data);
                    },
                    error: function (error) {
                        reject("load request failed: " + error.message);
                    }
                });
            });
        });
}


// 펀딩 디테일 객체 혹은 학생 게시글 디테일 객체를 불러오는 함수 (POST)
export function insertObj(url, requestData) {
    return jwtToken.getUserToken()
        .then(jwtToken => {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "POST",
                    data: JSON.stringify(requestData),
                    url: url,
                    dataType: "json",
                    contentType: 'application/json',
                    cache: false,
                    headers: {
                        "Authorization": "Bearer " + jwtToken
                    },
                    success: function (data) {
                        resolve(data);
                    },
                    error: function (error) {
                        reject("request failed: " + error.message);
                    }
                });
            });
        });
}



// 객체 삭제 요청 함수
export function deleteObj(url, requestData) {
    return jwtToken.getUserToken()
        .then(jwtToken => {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "DELETE",
                    url: url,
                    cache: false,
                    headers: {
                        "Authorization": "Bearer " + jwtToken
                    },
                    success: function (data) {
                        resolve(data);
                    },
                    error: function (error) {
                        reject("delete request failed: " + error.message);
                    }
                });
            });
        });
}





export class loadCall {
    constructor(url, requestData) {
        this.detail = loadDetailObj(url, requestData);

        this.detail
            .then(data => {
                this.data = data;
            })
            .catch(error => {
                // 오류 처리
                console.log(error);
                alert("오류가 발생했습니다. 오류 메시지: " + error);류
            });
    }
}

export class insertCall {
    constructor(url, requestData) {
        this.detail = insertObj(url, requestData);

        this.detail
            .then(data => {
                this.data = data;
            })
            .catch(error => {
                // 오류 처리
                console.log(error);
                alert("오류가 발생했습니다. 오류 메시지: " + error);
            });
    }
}


export class deleteCall {
    constructor(url, requestData) {
        this.detail = deleteObj(url, requestData);

        this.detail
            .then(data => {
                this.data = data;
            })
            .catch(error => {
                // 오류 처리
                console.log(error);
                alert("오류가 발생했습니다. 오류 메시지: " + error);
            });
    }
}

export class payCall {
    constructor(url, requestData) {
        this.detail = loadDetailObj(url, requestData);

        this.detail
            .then(data => {
                this.data = data;
            })
            .catch(error => {
                // 오류 처리
                console.log(error);
                alert("오류가 발생했습니다. 오류 메시지: " + error);
            });
    }
}