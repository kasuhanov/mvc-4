app.controller('VerificationController', function($scope, $stateParams, $http,$rootScope) {
    $rootScope.title = "Подтверждение регистрации";
    $http.get("verify/"+$stateParams.token)
        .success(function(){
            $scope.success = true;
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
    $scope.resend = function (){
        $scope.loading = true;
        $http.get("resend/"+$stateParams.token)
            .success(function(){
                $scope.sendSuccess = true;
                $scope.loading = false;
            })
            .error(function(){
                $scope.failed = true;
                $scope.loading = false;
            });
    }
});