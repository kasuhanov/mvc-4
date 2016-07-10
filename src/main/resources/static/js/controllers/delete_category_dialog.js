app.controller('DeleteCategoryDialogController', function($scope, $state, $http, $uibModalInstance) {
    init();
    function init() {
        $http.get('category/all')
            .success(function(data) {
                $scope.categories = data;
            });
    }
    $scope.submit = function(){
        $scope.failed = false;
        $scope.success = false;
        $http.delete('category/' + $scope.category)
            .success(function () {
                $scope.failed = false;
                $scope.success = true;
               init();
            })
            .error(function () {
                $scope.failed = true;
                $scope.success = false;
            });
    };
    $scope.closeDialog = function(){
        $uibModalInstance.dismiss();
    };
});
