app.controller('RecoveryController', function($scope, $rootScope, $http){
    $rootScope.title = "Восстановление пароля";
    $scope.user = {
        email:""
    };
    $scope.recover = function(){
        $scope.loading = true;
        $scope.noSuchEmail = false;
        $scope.failed = false;
        $scope.success = false;
        $http.post("recovery",$scope.user.email)
            .success(function(){
                $scope.success = true;
                $scope.loading = false;
            })
            .error(function(data, status){
                if(status == 409){
                    $scope.noSuchEmail = true;
                }else{
                    $scope.failed = true;
                }
                $scope.loading = false;
            });
    };
});