app.controller('ErrorController', function($scope, $http, $stateParams, $state, $rootScope,paginate) {
    $scope.message = $stateParams.message;
    $scope.code = $stateParams.code;
});