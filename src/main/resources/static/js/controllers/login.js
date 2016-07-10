app.controller('LoginController', function($scope, $http, $rootScope) {
    $scope.user = {
        email:"",
        password:""
    };
    $scope.login = function(){
        $scope.loading = true;
        $rootScope.credentials.username = $scope.user.email;
        $rootScope.credentials.password = $scope.user.password;
        $rootScope.authenticate($rootScope.credentials, function(authenticated) {
            if (authenticated) {
                console.log("Login succeeded");
                $rootScope.authenticated = true;
                $scope.failed = false;
            } else {
                console.log("Login failed");
                $rootScope.authenticated = false;
                $scope.failed = true;
            }
            $scope.loading = false;
        })
    };
});