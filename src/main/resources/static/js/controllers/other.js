app.controller('OtherController', function($scope, $http, $stateParams, $state, $rootScope, paginate) {
    $rootScope.title = "Прочее";
    $scope.url='purchase/other/page?pageSize=';
    $scope.currentPage =  $stateParams.page || 1;
    paginate($scope,$http,$state,$stateParams);
    init($stateParams.order, $stateParams.orderby);

    function init(order, orderby){
        $scope.orderby = orderby===undefined?'submissionCloseDate':orderby;
        $scope.order = order===undefined?'desc':order;
        $scope.url='purchase/other/page?order='+$scope.order+'&orderby='+$scope.orderby+'&pageSize=';
        $scope.currentPage =  $stateParams.page || 1;
        paginate($scope,$http,$state,$stateParams);
    }

    $scope.changeOrder = function(orderby){
        var stateParams = $stateParams;
        stateParams.orderby = orderby;
        if($scope.orderby == orderby){
            if($scope.order == 'asc')
                stateParams.order = 'desc';
            else
                stateParams.order = 'asc';
        }else{
            stateParams.order = 'asc';
        }
        stateParams.page = 1;
        init(stateParams.order, stateParams.orderby);
        $state.transitionTo($state.current.name,stateParams,{notify:false});
    }
});