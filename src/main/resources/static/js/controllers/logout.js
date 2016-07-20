app.controller('LogoutController', function($scope, $rootScope, $http, $window){
    $scope.logOut = function(){
        $http.post('logout', {}).success(function() {
            $rootScope.authenticated = false;
            $window.location.reload();
        }).error(function() {
            $rootScope.authenticated = false;
            $window.location.reload();
        });
    };
});