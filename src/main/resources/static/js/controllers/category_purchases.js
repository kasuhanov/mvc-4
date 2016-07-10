app.controller('CategoryPurchasesController', function($scope, $http, $stateParams, $state, $rootScope, paginate) {
    $rootScope.title = "Закупки";
    $scope.url = 'category/' + $stateParams.id + '/purchases?pageSize=';

    $http.get("category/"+$stateParams.id)
        .success(function(data) {
            $scope.category = data;
            $rootScope.title = $scope.category.name;
        });

    init($stateParams.order, $stateParams.orderby);

    function init(order, orderby){
        $scope.orderby = orderby===undefined?'submissionCloseDate':orderby;
        $scope.order = order===undefined?'desc':order;
        $scope.url='category/' + $stateParams.id + '/purchases?order='+$scope.order
            +'&orderby='+$scope.orderby+'&pageSize=';
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