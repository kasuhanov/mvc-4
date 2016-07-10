var admin = angular.module('admin',[]);

admin.controller('MainController', function($scope, $http, $window){
    $scope.authenticate = function() {
        $http.post('user-admin', $scope.admin)
            .success(function(data) {
                $window.location = "/";
            })
            .error(function() {
                $scope.failed = true;
            });
    };
});