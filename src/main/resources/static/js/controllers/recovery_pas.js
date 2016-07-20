app.controller('RecoveryPasController', function($scope, $stateParams, $rootScope, $http){
    $rootScope.title = "Восстановление пароля";
    $scope.user = {
        password:"",
        confirmPassword:""
    };
    $http.get("verify/recovery/"+$stateParams.token)
        .success(function(data){
        })
        .error(function(data, status){
            if(status == 409){
                $scope.timeout = true;
            }else if(status == 404){
                $scope.notFound = true;
            }else{
                $scope.failed = true;
            }
        });
    $scope.recover = function(){
        $scope.loading = true;
        $scope.timeout = false;
        $scope.failed = false;
        $scope.success = false;
        $http.post("recovery/" + $stateParams.token, $scope.user.password)
            .success(function(){
                $scope.success = true;
                $scope.loading = false;
            })
            .error(function(data, status){
                if(status == 409){
                    $scope.timeout = true;
                }else{
                    $scope.failed = true;
                }
                $scope.loading = false;
            });
    };
    $scope.resend = function (){
        $scope.loading = true;
        $http.get("resend-password/"+$stateParams.token)
            .success(function(){
                $scope.sendSuccess = true;
                $scope.loading = false;
            })
            .error(function(){
                $scope.failed = true;
                $scope.loading = false;
            });
    };
});