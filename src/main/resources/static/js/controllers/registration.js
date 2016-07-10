app.controller('RegistrationController', function($scope, $rootScope, $http){
    $rootScope.title = "Регистрация";
    $scope.user = {
        email:"",
        password:"",
        confirmPassword:""
    };
    $scope.registrate = function(){
        $scope.loading = true;
        $scope.emailExists = false;
        $scope.failed = false;
        $scope.success = false;
        $http.post("registrate", $scope.user)
            .success(function(){
                $scope.success = true;
                $scope.loading = false;
            })
            .error(function(data, status){
                if(status == 409){
                    $scope.emailExists = true;
                }else{
                    $scope.failed = true;
                }
                $scope.loading = false;
            });
    };
});